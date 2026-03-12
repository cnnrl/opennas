package com.conner.gdrive.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.conner.gdrive.models.AuditEntry;
import com.conner.gdrive.services.AuditService;
import com.conner.gdrive.services.JwtService;

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

  @Around("execution(* com.conner.gdrive.GdriveController.*(..))")
  public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
    // before
    Object result = joinPoint.proceed(); // actually calls the method
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
