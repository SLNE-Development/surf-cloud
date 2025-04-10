# SSL Certificate Generation Guide for Netty Server and Clients

This guide explains how to generate and manage SSL/TLS certificates for your Netty-based server and
clients using OpenSSL. Properly generated certificates ensure secure communication between your
server and clients.

> **Note:** If you already have a server set up and only need to add a new client, you can skip directly
> to **[Step 4](#step-4-generate-client-certificates-repeatable-for-each-client)**. Ensure the following files are already present in your working directory:
> - `openssl.cnf`
> - `ca.key`
> - `ca.crt`

---

## Prerequisites

- Install [OpenSSL](https://slproweb.com/products/Win32OpenSSL.html) or use WSL/Git Bash.
- Ensure OpenSSL is available in your command prompt:

```bash
openssl version
```

---

## Step-by-Step Guide

### Step 1: Create an OpenSSL Configuration File (`openssl.cnf`)

Create a file named `openssl.cnf` in your working directory:

```ini
[ req ]
default_bits       = 2048
distinguished_name = req_distinguished_name
req_extensions     = v3_req
prompt             = no

[ req_distinguished_name ]
C  = DE
ST = Remote
L  = Internet
O  = Surf Cloud
CN = YOUR_SERVER_IP_OR_DNS

[ v3_req ]
subjectAltName = @alt_names
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth

[ alt_names ]
IP.1 = YOUR_SERVER_IP
DNS.1 = YOUR_SERVER_DNS
```

Replace `YOUR_SERVER_IP` and `YOUR_SERVER_DNS` with your actual server IP address and DNS name.

Example:

```ini
CN = 192.168.1.23

[ alt_names ]
IP.1 = 192.168.1.23
DNS.1 = myserver.local
```

---

### Step 2: Generate Your Own Certificate Authority (CA)

Run the following commands once to create your CA:

```bash
openssl genrsa -out ca.key 2048

openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.crt -subj "/C=DE/ST=Remote/L=Internet/O=Surf Cloud/CN=SurfCloud CA"
```

**Important:**

- `ca.key`: Private key of your CA (keep this secret and safe).
- `ca.crt`: Public certificate of your CA (clients and server use this to establish trust).

---

### Step 3: Generate Server Certificate

```bash
# Generate private key
openssl genrsa -out server.key 2048

# Generate CSR (Certificate Signing Request)
openssl req -new -key server.key -out server.csr -config openssl.cnf

# Sign CSR with your CA
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 365 -extfile openssl.cnf -extensions v3_req
```

**Files produced:**

- `server.key`: Server's private key (use on your Netty server).
- `server.crt`: Server's signed certificate (distribute to clients).

---

### Step 4: Generate Client Certificates (repeatable for each client)

Modify `openssl.cnf` by changing the `CN` to your client name (`client01`, `client02`, etc.):

```ini
CN = test-server01
```

Then run:

```bash
openssl genrsa -out client.key 2048

openssl req -new -key client.key -out client.csr -config openssl.cnf

openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt -days 365 -extfile openssl.cnf -extensions v3_req
```

**Files produced:**

- `client.key`: Client's private key (keep secret and only on the client).
- `client.crt`: Client's signed certificate (put this on your server to authenticate the
  client).

---

## File Management

### Files to Keep and Their Usage

| File         | Purpose                                      | Store on                             |
|--------------|----------------------------------------------|--------------------------------------|
| `ca.key`     | CA private key (needed for signing)          | Secure offline storage (never share) |
| `ca.crt`     | CA certificate (trusted by all participants) | Server and Clients                   |
| `server.key` | Server private key                           | Server (secure)                      |
| `server.crt` | Server certificate (signed by CA)            | Server (send to clients)             |
| `client.key` | Client private key                           | Client (secure)                      |
| `client.crt` | Client certificate (signed by CA)            | Server (trust this certificate)      |

### Directory Structure

**On Server:**

```
certificates/
├── server.key
├── server.crt
└── ca.crt
```

**On Client:**

```
certificates/
├── client.key
├── client.crt
└── ca.crt
```

---

## Common Errors and Fixes

- **Hostname/IP undefined:** Ensure your server certificate includes the correct IP/DNS in SAN.
- **certificate_unknown:** Ensure the client certificate is correctly placed in
  `certificates/clients/` and signed by the CA.

---