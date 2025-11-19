$files = Get-ChildItem -Path "app/compose-chatgpt-kotlin-android-chatbot/app/src" -Filter "*.kt" -Recurse
foreach ($file in $files) {
    (Get-Content $file.FullName) | 
    ForEach-Object { $_ -replace "package com\.chatgptlite\.wanted", "package com.helpyourself.com" } |
    Set-Content $file.FullName
} 