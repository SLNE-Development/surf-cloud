package dev.slne.surf.cloud.api.common.meta

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Annotation for marking a class as a Netty packet in the Surf Cloud application.
 *
 * @property id The unique identifier of the packet.
 * @property flow The direction of the packet flow (e.g., client-to-server or server-to-client).
 * @property protocols The supported connection protocols for the packet. Defaults to [ConnectionProtocol.RUNNING].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SurfNettyPacket(
    val id: String,
    val flow: PacketFlow,
    @Internal vararg val protocols: ConnectionProtocol = [ConnectionProtocol.RUNNING]
)

/**
 * Annotation for marking properties for packet codec handling in the Surf Cloud application.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PacketCodec

/**
 * Object containing default packet IDs for various operations in the Surf Cloud application.
 */
object DefaultIds {

    const val PROXIED_NETTY_PACKET = "cloud:proxied"

    // Handshake
    const val SERVERBOUND_HANDSHAKE_PACKET = "cloud:serverbound:handshake"

    // Initialize
    const val CLIENTBOUND_INITIALIZE_CLIENT_PACKET = "cloud:clientbound:initialize_client"
    const val SERVERBOUND_INITIALIZE_REQUEST_ID_PACKET = "cloud:serverbound:initialize_request_id"
    const val CLIENTBOUND_INITIALIZE_ID_RESPONSE = "cloud:clientbound:initialize_id_response"

    // Login
    const val SERVERBOUND_LOGIN_START_PACKET = "cloud:serverbound:login_start"
    const val CLIENTBOUND_LOGIN_FINISHED_PACKET = "cloud:clientbound:login_finished"
    const val SERVERBOUND_LOGIN_ACKNOWLEDGED_PACKET = "cloud:serverbound:login_acknowledged"
    const val CLIENTBOUND_LOGIN_DISCONNECT_PACKET = "cloud:clientbound:login_disconnect"
    const val SERVERBOUND_KEY_PACKET =
        "cloud:serverbound:key" // Different protocol state so the ids can be overlapping
    const val CLIENTBOUND_KEY_PACKET = "cloud:clientbound:key"

    // Pre-Running
    const val SERVERBOUND_READY_TO_RUN_PACKET = "cloud:serverbound:ready_to_run"
    const val CLIENTBOUND_PRE_RUNNING_FINISHED_PACKET = "cloud:clientbound:pre_running_finished"
    const val SERVERBOUND_PRE_RUNNING_ACKNOWLEDGED_PACKET =
        "cloud:serverbound:pre_running_acknowledged"
    const val SERVERBOUND_REQUEST_CONTINUATION = "cloud:serverbound:request_continuation"
    const val CLIENTBOUND_READY_TO_RUN_PACKET = "cloud:clientbound:ready_to_run"

    // Running
    const val CLIENTBOUND_KEEP_ALIVE_PACKET = "cloud:clientbound:keep_alive"
    const val SERVERBOUND_KEEP_ALIVE_PACKET = "cloud:serverbound:keep_alive"

    const val CLIENTBOUND_PING_PACKET = "cloud:clientbound:ping"
    const val SERVERBOUND_PONG_PACKET = "cloud:serverbound:pong"

    const val SERVERBOUND_PING_REQUEST_PACKET = "cloud:serverbound:ping_request"
    const val CLIENTBOUND_PING_REQUEST_RESPONSE_PACKET = "cloud:clientbound:ping_request_response"

    const val SERVERBOUND_BROADCAST_PACKET = "cloud:serverbound:broadcast"
    const val CLIENTBOUND_DISCONNECT_PACKET = "cloud:clientbound:disconnect"

    const val SERVERBOUND_CLIENT_INFORMATION_PACKET = "cloud:serverbound:client_information"

    const val CLIENTBOUND_BUNDLE_DELIMITER_PACKET = "cloud:clientbound:bundle_delimiter"
    const val CLIENTBOUND_BUNDLE_PACKET = "cloud:clientbound:bundle"
    const val SERVERBOUND_BUNDLE_DELIMITER_PACKET = "cloud:serverbound:bundle_delimiter"
    const val SERVERBOUND_BUNDLE_PACKET = "cloud:serverbound:bundle"

    const val SERVERBOUND_SEND_MESSAGE_PACKET = "cloud:serverbound:send_message"
    const val CLIENTBOUND_SEND_MESSAGE_PACKET = "cloud:clientbound:send_message"

    const val SERVERBOUND_SEND_ACTION_BAR_PACKET = "cloud:serverbound:send_action_bar"
    const val CLIENTBOUND_ACTION_BAR_PACKET = "cloud:clientbound:action_bar"

    const val SERVERBOUND_SEND_PLAYER_LIST_HEADER_AND_FOOTER =
        "cloud:serverbound:send_player_list_header_and_footer"
    const val CLIENTBOUND_PLAYER_LIST_HEADER_AND_FOOTER =
        "cloud:clientbound:player_list_header_and_footer"

    const val SERVERBOUND_SEND_TITLE_PACKET = "cloud:serverbound:send_title"
    const val CLIENTBOUND_TITLE_PACKET = "cloud:clientbound:title"

    const val SERVERBOUND_SEND_TITLE_PART_PACKET = "cloud:serverbound:send_title_part"
    const val CLIENTBOUND_TITLE_PART_PACKET = "cloud:clientbound:title_part"

    const val SERVERBOUND_CLEAR_TITLE_PACKET = "cloud:serverbound:clear_title"
    const val CLIENTBOUND_CLEAR_TITLE_PACKET = "cloud:clientbound:clear_title"

    const val SERVERBOUND_RESET_TITLE_PACKET = "cloud:serverbound:reset_title"
    const val CLIENTBOUND_RESET_TITLE_PACKET = "cloud:clientbound:reset_title"

    const val SERVERBOUND_SHOW_BOSS_BAR_PACKET = "cloud:serverbound:show_boss_bar"
    const val CLIENTBOUND_SHOW_BOSS_BAR_PACKET = "cloud:clientbound:show_boss_bar"

    const val SERVERBOUND_HIDE_BOSS_BAR_PACKET = "cloud:serverbound:hide_boss_bar"
    const val CLIENTBOUND_HIDE_BOSS_BAR_PACKET = "cloud:clientbound:hide_boss_bar"

    const val SERVERBOUND_PLAY_SOUND_PACKET = "cloud:serverbound:play_sound"
    const val CLIENTBOUND_PLAY_SOUND_PACKET = "cloud:clientbound:play_sound"

    const val SERVERBOUND_STOP_SOUND_PACKET = "cloud:serverbound:stop_sound"
    const val CLIENTBOUND_STOP_SOUND_PACKET = "cloud:clientbound:stop_sound"

    const val SERVERBOUND_OPEN_BOOK_PACKET = "cloud:serverbound:open_book"
    const val CLIENTBOUND_OPEN_BOOK_PACKET = "cloud:clientbound:open_book"

    const val SERVERBOUND_SEND_RESOURCE_PACKS_PACKET = "cloud:serverbound:send_resource_packs"
    const val CLIENTBOUND_SEND_RESOURCE_PACKS_PACKET = "cloud:clientbound:send_resource_packs"

    const val SERVERBOUND_REMOVE_RESOURCE_PACKS_PACKET = "cloud:serverbound:remove_resource_packs"
    const val CLIENTBOUND_REMOVE_RESOURCE_PACKS_PACKET = "cloud:clientbound:remove_resource_packs"

    const val SERVERBOUND_CLEAR_RESOURCE_PACKS_PACKET = "cloud:serverbound:clear_resource_packs"
    const val CLIENTBOUND_CLEAR_RESOURCE_PACKS_PACKET = "cloud:clientbound:clear_resource_packs"

    const val PLAYER_CONNECT_TO_SERVER_PACKET = "cloud:player:connect_to_server"
    const val PLAYER_DISCONNECT_FROM_SERVER_PACKET = "cloud:player:disconnect_from_server"

    const val SERVERBOUND_REQUEST_DISPLAY_NAME_PACKET = "cloud:serverbound:request_display_name"
    const val CLIENTBOUND_REQUEST_DISPLAY_NAME_PACKET = "cloud:clientbound:request_display_name"
    const val RESPONSE_DISPLAY_NAME_PACKET_REQUEST_PACKET =
        "cloud:response:display_name_packet_request"

    const val CLIENTBOUND_REGISTER_SERVER_PACKET = "cloud:clientbound:register_server"
    const val CLIENTBOUND_UNREGISTER_SERVER_PACKET = "cloud:clientbound:unregister_server"

    const val CLIENTBOUND_UPDATE_SERVER_INFORMATION_PACKET =
        "cloud:clientbound:update_server_information"

    const val CLIENTBOUND_ADD_PLAYER_TO_SERVER = "cloud:clientbound:add_player_to_server"
    const val CLIENTBOUND_REMOVE_PLAYER_FROM_SERVER = "cloud:clientbound:remove_player_from_server"

    const val SERVERBOUND_IS_SERVER_MANAGED_BY_THIS_PROXY_REQUEST =
        "cloud:serverbound:is_server_managed_by_this_proxy_request"
    const val CLIENTBOUND_IS_SERVER_MANAGED_BY_THIS_PROXY_RESPONSE =
        "cloud:clientbound:is_server_managed_by_this_proxy_response"

    const val CLIENTBOUND_TRANSFER_PLAYER_PACKET = "cloud:clientbound:transfer_player"
    const val SERVERBOUND_TRANSFER_PLAYER_PACKET_RESPONSE_PACKET =
        "cloud:serverbound:transfer_player_packet_response"

    const val REQUEST_LUCKPERMS_META_DATA_PACKET = "cloud:request:luckperms_meta_data"
    const val LUCKPERMS_META_DATA_RESPONSE_PACKET = "cloud:response:luckperms_meta_data"

    const val SERVERBOUND_REQUEST_PLAYER_PERSISTENT_DATA_CONTAINER =
        "cloud:serverbound:request_player_persistent_data_container"
    const val CLIENTBOUND_PLAYER_PERSISTENT_DATA_CONTAINER_RESPONSE =
        "cloud:clientbound:player_persistent_data_container_response"
    const val SERVERBOUND_PLAYER_PERSISTENT_DATA_CONTAINER_UPDATE =
        "cloud:serverbound:player_persistent_data_container_update"

    const val SERVERBOUND_CONNECT_PLAYER_TO_SERVER = "cloud:serverbound:connect_player_to_server"
    const val CLIENTBOUND_CONNECT_PLAYER_TO_SERVER_RESPONSE =
        "cloud:clientbound:connect_player_to_server_response"

    const val BIDIRECTIONAL_DISCONNECT_PLAYER = "cloud:bidirectional:disconnect_player"

    const val BIDIRECTIONAL_TELEPORT_PLAYER = "cloud:bidirectional:teleport_player"
    const val BIDIRECTIONAL_TELEPORT_PLAYER_RESULT = "cloud:bidirectional:teleport_player_result"

    const val CLIENTBOUND_REGISTER_CLOUD_SERVERS_TO_PROXY =
        "cloud:clientbound:register_cloud_servers_to_proxy"

    const val CLIENTBOUND_SHUTDOWN_PACKET = "cloud:clientbound:shutdown"
    const val SERVERBOUND_SHUTDOWN_SERVER_PACKET = "cloud:serverbound:shutdown_server"

    const val CLIENTBOUND_BATCH_UPDATE_SERVER = "cloud:clientbound:batch_update_server"

    const val REQUEST_OFFLINE_DISPLAY_NAME_PACKET = "cloud:request:offline_display_name"
    const val RESPONSE_REQUEST_OFFLINE_DISPLAY_NAME_PACKET = "cloud:response:request_offline_display_name"

}
