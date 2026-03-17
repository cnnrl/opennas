package io.github.cnnrl.opennas.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import io.github.cnnrl.opennas.models.AuditEntry;
import io.github.cnnrl.opennas.services.AuditService;
import io.github.cnnrl.opennas.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {
  private final AuditService auditService;
  private final JwtService jwtService;

  public AuditAspect(AuditService auditService, JwtService jwtService) {
    this.auditService = auditService;
    this.jwtService = jwtService;
  }

  @Around("execution(* io.github.cnnrl.opennas.controllers.*.*(..))")
  public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
    // before
    Object result = joinPoint.proceed();
    HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
        .currentRequestAttributes())
        .getRequest();
    String filename = "";
    Object[] args = joinPoint.getArgs();

    if (args.length > 0 && args[0] instanceof MultipartFile f) {
      filename = f.getOriginalFilename();
    } else if (args.length > 0 && args[0] instanceof String s) {
      filename = s;
    }

    String authHeader = req.getHeader("Authorization");
    String user = "";
    boolean auth = false;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      try {
        user = jwtService.extractUsername(authHeader.substring(7));
        auth = true;
      } catch (Exception e) {
        auth = false;
      }
    }

    AuditEntry entry = AuditEntry.of(req.getRemoteAddr(),
        user,
        joinPoint.getSignature().getName(),
        filename,
        auth);

    auditService.log(entry);
    // after
    return result;
  }
}
