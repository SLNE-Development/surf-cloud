#!/bin/bash

# Directory for certificates
CERTS_DIR="certificates"
SERVER_KEY="$CERTS_DIR/server.key"
SERVER_CRT="$CERTS_DIR/server.crt"

# Create the directory if it doesn't exist
mkdir -p "$CERTS_DIR"

# Generate the server key and certificate if they don't already exist
if [ ! -f "$SERVER_KEY" ] || [ ! -f "$SERVER_CRT" ]; then
    echo "Generating server key and certificate..."
    openssl genpkey -algorithm RSA -out "$SERVER_KEY" -pkeyopt rsa_keygen_bits:2048
    openssl req -new -key "$SERVER_KEY" -out "$CERTS_DIR/server.csr" -subj "/CN=Server"
    openssl x509 -req -days 365 -in "$CERTS_DIR/server.csr" -signkey "$SERVER_KEY" -out "$SERVER_CRT"
    rm "$CERTS_DIR/server.csr" # Remove the CSR file as it is no longer needed
else
    echo "Server key and certificate already exist."
fi

# Ask the user for the client's name
read -p "Enter the name of the client: " CLIENT_NAME

# File names for the client's key and certificate
CLIENT_KEY="$CERTS_DIR/$CLIENT_NAME.key"
CLIENT_CRT="$CERTS_DIR/$CLIENT_NAME.crt"

# Generate the client key and certificate
if [ -f "$CLIENT_KEY" ] && [ -f "$CLIENT_CRT" ]; then
    echo "Client key and certificate for '$CLIENT_NAME' already exist."
else
    echo "Generating client key and certificate for '$CLIENT_NAME'..."
    openssl genpkey -algorithm RSA -out "$CLIENT_KEY" -pkeyopt rsa_keygen_bits:2048
    openssl req -new -key "$CLIENT_KEY" -out "$CERTS_DIR/$CLIENT_NAME.csr" -subj "/CN=$CLIENT_NAME"
    openssl x509 -req -days 365 -in "$CERTS_DIR/$CLIENT_NAME.csr" -signkey "$CLIENT_KEY" -out "$CLIENT_CRT"
    rm "$CERTS_DIR/$CLIENT_NAME.csr" # Remove the CSR file as it is no longer needed
fi

echo "All keys and certificates have been saved in the '$CERTS_DIR' directory."
