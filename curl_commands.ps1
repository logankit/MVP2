# 1. Get Okta Token
Write-Host "Getting Okta Token..."
curl -k -X POST `
  "https://efxalpha-icg-us-gcp.okta.com/oauth2/ausuzpqio4JeHecUL5d6/v1/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -u "00acz0JosiExkPRwS5d7:h9QiEUraqvjQjxX2aPU-6NLxiTK4GskWOd7L8R05FmlrOMMHItFYMaNth_Fi5dg" `
  -d "grant_type=password" `
  -d "username=axp461" `
  -d "password=@2025Equifax"

Write-Host "`nAfter getting the token, replace YOUR_TOKEN_HERE in the command below with the access_token from above response"

# 2. Call Rules Editor API (replace YOUR_TOKEN_HERE with the token from above)
Write-Host "`nCalling Rules Editor API..."
curl -k -X POST `
  "https://c2o-rules-editor-service.dev.us-east-1.aws.interconnect.equifax.com/api/v1/rules" `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN_HERE" `
  -d "{\"ReferenceFFID\": {\"NewBillTo\": false, \"ActiveChargeOffers\": [], \"ActiveChargeOffersTypes\": [], \"C2oGTMEfxID\": \"your_id\", \"SfdGTMEfxID\": \"your_id\", \"DSGs\": [], \"FeeChargeOffers\": []}}"
