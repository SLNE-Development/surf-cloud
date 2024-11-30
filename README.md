[![Netlify Status](https://api.netlify.com/api/v1/badges/faf8d5de-85be-4d57-ba0f-bab1e058808f/deploy-status)](https://app.netlify.com/sites/surf-cloud-docs/deploys)

Docs: [https://surf-cloud-docs.netlify.app/](https://surf-cloud-docs.netlify.app/)

# System properties properties:
- 


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
    - 