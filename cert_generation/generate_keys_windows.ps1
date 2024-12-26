# Check if WSL is installed
if (!(Get-Command wsl -ErrorAction SilentlyContinue)) {
    Write-Host "WSL is not installed. Installing WSL..."
    Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Windows-Subsystem-Linux -NoRestart
    Enable-WindowsOptionalFeature -Online -FeatureName VirtualMachinePlatform -NoRestart
    Write-Host "WSL installed. Please restart your computer and run this script again." -ForegroundColor Yellow
    exit
}

# Check if Ubuntu is installed
if (!(wsl -l -q | Select-String -Pattern "Ubuntu")) {
    Write-Host "Installing Ubuntu..."
    wsl --install -d Ubuntu
    Write-Host "Ubuntu installation complete. Please complete the setup in the opened Ubuntu console, then run this script again." -ForegroundColor Yellow
    exit
}

# Ensure WSL is set to version 2
Write-Host "Ensuring WSL is set to version 2..."
wsl --set-default-version 2

# Install OpenSSL in Ubuntu
Write-Host "Updating and installing dependencies in WSL..."
wsl sudo apt update && wsl sudo apt install -y openssl

# Copy the Bash script to WSL
Write-Host "Copying the Bash script to WSL..."
$scriptPath = "$PWD\generate_keys.sh"
if (!(Test-Path $scriptPath)) {
    Write-Host "Bash script 'generate_keys.sh' not found in the current directory." -ForegroundColor Red
    exit 1
}
wsl cp $(wslpath -a $scriptPath) ~

# Make the script executable and run it
Write-Host "Running the script in WSL..."
wsl chmod +x ~/generate_keys.sh
wsl ~/generate_keys.sh