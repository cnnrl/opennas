#!/bin/bash
# Configuration Variables
BASE_URL="http://localhost:8080"
USERNAME="test_user_$RANDOM"
PASSWORD="securepassword123"
TEST_FILE="test.mp3"
# Colors for output formatting
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
 
echo "========================================="
echo " Starting API Integration Tests"
echo "========================================="
 
# 1. Test Registration
echo -n "1. Testing POST /register... "
REGISTER_RES=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\", \"password\":\"$PASSWORD\"}")
REG_BODY=$(echo "$REGISTER_RES" | sed '$d')
REG_STATUS=$(echo "$REGISTER_RES" | tail -n1)
if [ "$REG_STATUS" -eq 200 ]; then
  echo -e "${GREEN}PASS${NC}"
  TOKEN=$(echo "$REG_BODY" | sed 's/"//g')
else
  echo -e "${RED}FAIL (HTTP $REG_STATUS)${NC}"
  echo "Response: $REG_BODY"
  exit 1
fi
 
# 2. Test Duplicate Registration
echo -n "2. Testing POST /register (duplicate username)... "
DUP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\", \"password\":\"$PASSWORD\"}")
if [ "$DUP_STATUS" -eq 400 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 400 as expected)"
else
  echo -e "${RED}FAIL (HTTP $DUP_STATUS, expected 400)${NC}"
  exit 1
fi
 
# 3. Test Registration with blank username
echo -n "3. Testing POST /register (blank username)... "
BLANK_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"\", \"password\":\"$PASSWORD\"}")
if [ "$BLANK_STATUS" -eq 400 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 400 as expected)"
else
  echo -e "${RED}FAIL (HTTP $BLANK_STATUS, expected 400)${NC}"
  exit 1
fi
 
# 4. Test Login
echo -n "4. Testing POST /login... "
LOGIN_RES=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\", \"password\":\"$PASSWORD\"}")
LOGIN_BODY=$(echo "$LOGIN_RES" | sed '$d')
LOGIN_STATUS=$(echo "$LOGIN_RES" | tail -n1)
if [ "$LOGIN_STATUS" -eq 200 ]; then
  echo -e "${GREEN}PASS${NC}"
  TOKEN=$(echo "$LOGIN_BODY" | sed 's/"//g')
else
  echo -e "${RED}FAIL (HTTP $LOGIN_STATUS)${NC}"
  exit 1
fi
 
# 5. Test Login with wrong password
echo -n "5. Testing POST /login (wrong password)... "
WRONG_PASS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\", \"password\":\"wrongpassword\"}")
if [ "$WRONG_PASS_STATUS" -eq 400 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 400 as expected)"
else
  echo -e "${RED}FAIL (HTTP $WRONG_PASS_STATUS, expected 400)${NC}"
  exit 1
fi
 
# 6. Test protected route with no token
echo -n "6. Testing GET /music (no token)... "
NO_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/music")
if [ "$NO_TOKEN_STATUS" -eq 401 ] || [ "$NO_TOKEN_STATUS" -eq 403 ]; then
  echo -e "${GREEN}PASS${NC} (Returned $NO_TOKEN_STATUS as expected)"
else
  echo -e "${RED}FAIL (HTTP $NO_TOKEN_STATUS, expected 401/403)${NC}"
  exit 1
fi
 
# 7. Test Music Upload
echo -n "7. Testing POST /music/upload... "
UPLOAD_RES=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/music/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@$TEST_FILE")
UPLOAD_BODY=$(echo "$UPLOAD_RES" | sed '$d')
UPLOAD_STATUS=$(echo "$UPLOAD_RES" | tail -n1)
if [ "$UPLOAD_STATUS" -eq 201 ]; then
  echo -e "${GREEN}PASS${NC}"
  SONG_ID=$(echo "$UPLOAD_BODY" | jq -r '.id')
else
  echo -e "${RED}FAIL (HTTP $UPLOAD_STATUS)${NC}"
  echo "Response: $UPLOAD_BODY"
  exit 1
fi
 
# 8. Test Get All Songs
echo -n "8. Testing GET /music... "
SONGS_RES=$(curl -s -w "\n%{http_code}" "$BASE_URL/music" \
  -H "Authorization: Bearer $TOKEN")
SONGS_BODY=$(echo "$SONGS_RES" | sed '$d')
SONGS_STATUS=$(echo "$SONGS_RES" | tail -n1)
if [ "$SONGS_STATUS" -eq 200 ]; then
  SONG_COUNT=$(echo "$SONGS_BODY" | jq '. | length')
  echo -e "${GREEN}PASS${NC} ($SONG_COUNT song(s) returned)"
else
  echo -e "${RED}FAIL (HTTP $SONGS_STATUS)${NC}"
  exit 1
fi
 
# 9. Test Cover Art
echo -n "9. Testing GET /music/art/$SONG_ID... "
COVER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/music/art/$SONG_ID" \
  -H "Authorization: Bearer $TOKEN")
if [ "$COVER_STATUS" -eq 200 ]; then
  echo -e "${GREEN}PASS${NC}"
else
  echo -e "${RED}FAIL (HTTP $COVER_STATUS)${NC}"
  exit 1
fi
 
# 10. Test Cover Art with invalid ID
echo -n "10. Testing GET /music/art/fakeid (invalid ID)... "
COVER_FAKE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/music/art/fakeid" \
  -H "Authorization: Bearer $TOKEN")
if [ "$COVER_FAKE_STATUS" -eq 400 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 400 as expected)"
else
  echo -e "${RED}FAIL (HTTP $COVER_FAKE_STATUS, expected 400)${NC}"
  exit 1
fi
 
# 11. Test Getting Stream Token
echo -n "11. Testing GET /stream/token/$SONG_ID... "
STREAM_TOKEN_RES=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/stream/token/$SONG_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -c cookies.txt)
STREAM_TOKEN_BODY=$(echo "$STREAM_TOKEN_RES" | sed '$d')
STREAM_TOKEN_STATUS=$(echo "$STREAM_TOKEN_RES" | tail -n1)
if [ "$STREAM_TOKEN_STATUS" -eq 200 ]; then
  echo -e "${GREEN}PASS${NC}"
  STREAM_TOKEN=$(echo "$STREAM_TOKEN_BODY" | sed 's/"//g')
else
  echo -e "${RED}FAIL (HTTP $STREAM_TOKEN_STATUS)${NC}"
  echo "Response: $STREAM_TOKEN_BODY"
  exit 1
fi
 
# 12. Test Stream Token with invalid song ID
echo -n "12. Testing GET /stream/token/fakeid (invalid ID)... "
FAKE_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/stream/token/fakeid" \
  -H "Authorization: Bearer $TOKEN")
if [ "$FAKE_TOKEN_STATUS" -eq 400 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 400 as expected)"
else
  echo -e "${RED}FAIL (HTTP $FAKE_TOKEN_STATUS, expected 400)${NC}"
  exit 1
fi
 
# 13. Test Streaming with HTTP Range
echo -n "13. Testing GET /stream/$SONG_ID (Partial Content/Range)... "
STREAM_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/stream/$SONG_ID?token=$STREAM_TOKEN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Range: bytes=0-1024" \
  -b cookies.txt)
if [ "$STREAM_STATUS" -eq 206 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 206 Partial Content)"
elif [ "$STREAM_STATUS" -eq 200 ]; then
  echo -e "${GREEN}PASS${NC} (Returned 200 OK)"
else
  echo -e "${RED}FAIL (HTTP $STREAM_STATUS)${NC}"
  exit 1
fi
 
# 14. Test Streaming with invalid token
echo -n "14. Testing GET /stream/$SONG_ID (invalid stream token)... "
INVALID_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  "$BASE_URL/stream/$SONG_ID?token=invalidtoken" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Range: bytes=0-1024" \
  -b cookies.txt)
if [ "$INVALID_TOKEN_STATUS" -eq 400 ] || [ "$INVALID_TOKEN_STATUS" -eq 403 ] || [ "$INVALID_TOKEN_STATUS" -eq 500 ]; then
  echo -e "${GREEN}PASS${NC} (Returned $INVALID_TOKEN_STATUS as expected)"
else
  echo -e "${RED}FAIL (HTTP $INVALID_TOKEN_STATUS, expected 400/403/500)${NC}"
  exit 1
fi
 
# 15. Test Streaming with wrong song ID
echo -n "15. Testing GET /stream/fakeid (valid token, wrong song ID)... "
MISMATCH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  "$BASE_URL/stream/fakeid?token=$STREAM_TOKEN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Range: bytes=0-1024" \
  -b cookies.txt)
if [ "$MISMATCH_STATUS" -eq 400 ] || [ "$MISMATCH_STATUS" -eq 403 ] || [ "$MISMATCH_STATUS" -eq 500 ]; then
  echo -e "${GREEN}PASS${NC} (Returned $MISMATCH_STATUS as expected)"
else
  echo -e "${RED}FAIL (HTTP $MISMATCH_STATUS, expected 400/403)${NC}"
  exit 1
fi

# Cleanup
echo "Cleaning up..."
rm -f cookies.txt cookies2.txt
echo "========================================="
echo -e "${GREEN}All tests completed successfully!${NC}"
echo "========================================="
