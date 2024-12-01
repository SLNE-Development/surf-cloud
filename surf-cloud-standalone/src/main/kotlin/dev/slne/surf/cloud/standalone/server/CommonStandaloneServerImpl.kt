package dev.slne.surf.cloud.standalone.server

//abstract class CommonStandaloneServerImpl(
//    uid: Long,
//    group: String,
//    name: String,
//    users: UserListImpl,
//    information: ClientInformation,
//    override val connection: Connection,
//) : CommonCloudServerImpl(uid, group, name, users, information ), ServerCommonCloudServer {
//    private val connectionThrottlerer = Mutex()
//
//    fun addPlayer(player: CloudPlayer) {
//        users.add(player)
//    }
//
//    fun removePlayer(player: CloudPlayer) {
//        users.remove(player)
//    }
//
//    suspend fun connectPlayer(player: StandaloneCloudPlayerImpl): ConnectionResult {
//        connectionThrottlerer.withLock {
//            if (player.server == this || player.proxyServer == this) {
//                return ConnectionResult.ALREADY_CONNECTED
//            }
//
//            if (emptySlots <= 0) { // TODO: 26.11.2024 20:36 - add bypass perm
//                return ConnectionResult.SERVER_FULL
//            }
//        }
//    }
//}