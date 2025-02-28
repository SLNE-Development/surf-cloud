[![Netlify Status](https://api.netlify.com/api/v1/badges/faf8d5de-85be-4d57-ba0f-bab1e058808f/deploy-status)](https://app.netlify.com/sites/surf-cloud-docs/deploys)

Docs: [https://surf-cloud-docs.netlify.app/](https://surf-cloud-docs.netlify.app/)

![logo](https://github.com/SLNE-Development/assets/blob/master/logos/surf-cloud/surf-cloud-logo-bg-removed.png?raw=true)


# TODO
## IntelliJ Plugin
- [ ] Inspections
    - [ ] Packet class Inspection
        - [ ] Annotated with @SurfNettyPacket
        - [ ] has accessible packet codec
        - [ ] follows conventions (has one private constructor with SurfByteBuf and one write methode `write(buf: SurfByteBuf)`)
        - [ ] extends `NettyPacket`
    - [ ] Packet listener inspection
        - [ ] method annotated with @SurfPacketListener has correct parameters
        - [ ] method annotated with @SurfPacketListener has correct return type
        - [ ] method annotated with @SurfPacketListener has correct access modifier (public only)
        - [ ] listener class is a spring component
    - [ ] when sending a response packet make sure the response packet is send via the `response()` method
- [ ] Project generator wizard
    - [ ] minecraft version
    - [ ] setup multi-gradle-project
        - [ ] api module (common, client, server)
        - [ ] core module (common, client, server)
        - [ ] client modules (paper, velocity)
        - [ ] server module (standalone)
    - [ ] add surf-api dependency
    - [ ] codestyle settings (only for java if anyone would ever use that)
    - [ ] configure dokka
    - [ ] configure gitlab ci
    - [ ] configure gitingore
    - [ ] basic project structure
- [ ] Generators
    - [ ] packet id generator (one class where all packet ids are stored, annotated with @PacketIdStore)
    - [ ] packet generator (generate basic packet class)

## Cloud
- [ ] Fragwürdiges timeout bei bootstrap auf client



# Setup

## First time:
1. start standalone and let the server generate the keys
2. start the client and let the client generate the folders (it will fail because the keys are not there)
3. Go back to the standalone and copy the `ca.pem`
4. Go back to the client and copy the `ca.pem` to the `certificates` folder
5. start the client again



## Generate certificates
````shell
wsl --install
````
````shell
sudo apt update
sudo apt install openssl
````
````shell
mkdir certificates
````

### Server Certificate
````shell
openssl genrsa -out certificates/server.key 2048
openssl req -new -x509 -key certificates/server.key -out certificates/server.crt -days 365 \
  -subj "/C=DE/ST=Berlin/L=Berlin/O=MyServer/OU=IT/CN=server.local"
````

### Client Certificate
Replace `{client-name}` with the name of the client

````shell
openssl genrsa -out certificates/{client-name}.key 2048
openssl req -new -key certificates/{client-name}.key -out certificates/{client-name}.csr \
  -subj "/C=DE/ST=Berlin/L=Berlin/O=MyClient/OU=IT/CN={client-name}.local"
openssl x509 -req -in certificates/{client-name}.csr -CA certificates/server.crt -CAkey certificates/server.key \
  -CAcreateserial -out certificates/{client-name}.crt -days 365
````
