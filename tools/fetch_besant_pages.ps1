<#
Downloads the 18 discourse pages of Annie Besant's "The Bhagavad-Gita" (4th edition, 1922) from English
Wikisource, exactly as Wikisource serves them, into .\besant_raw\ and zips them to .\besant_raw.zip.

Why only a download: the pages are parsed LATER, offline, against these real files, so a change in
Wikisource's page layout cannot silently mis-split verses.

Licence: Besant's translation (1922; she died in 1933) is public domain. The Wikisource transcription is
CC BY-SA 4.0 (credit: "English Wikisource, Bhagavad-Gita (Besant 4th)"). Be polite: 2 seconds between pages.

Run from the repository root:   powershell -ExecutionPolicy Bypass -File tools\fetch_besant_pages.ps1
#>
$ErrorActionPreference = 'Stop'
$ua = 'DailyDivine-content-build/1.0 (gaurav954624@gmail.com)'
$outDir = Join-Path (Get-Location) 'besant_raw'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$failed = @()
foreach ($n in 1..18) {
    $file = Join-Path $outDir ('discourse_{0:D2}.json' -f $n)
    $title = [uri]::EscapeDataString("Bhagavad-Gita (Besant 4th)/Discourse $n")
    $url = "https://en.wikisource.org/w/api.php?action=parse&page=$title&prop=text%7Crevid&format=json&formatversion=2"
    try {
        Invoke-WebRequest -Uri $url -UserAgent $ua -OutFile $file -UseBasicParsing
        $info = Get-Item $file
        $body = Get-Content -Raw -Encoding UTF8 $file
        if ($body -match '"error"' -or $info.Length -lt 2000) {
            Write-Warning ("Discourse {0}: the reply looks wrong ({1} bytes)" -f $n, $info.Length)
            $failed += $n
        } else {
            Write-Host ("Discourse {0,2}: {1,7} bytes" -f $n, $info.Length)
        }
    } catch {
        Write-Warning ("Discourse {0}: {1}" -f $n, $_.Exception.Message)
        $failed += $n
    }
    Start-Sleep -Seconds 2
}
$zip = Join-Path (Get-Location) 'besant_raw.zip'
Compress-Archive -Path $outDir -DestinationPath $zip -Force
if ($failed.Count -gt 0) {
    Write-Host ("Problems with discourse(s): {0}. Send me the zip anyway and the messages above." -f ($failed -join ', ')) -ForegroundColor Yellow
} else {
    Write-Host "All 18 pages downloaded. Send me besant_raw.zip." -ForegroundColor Green
}
