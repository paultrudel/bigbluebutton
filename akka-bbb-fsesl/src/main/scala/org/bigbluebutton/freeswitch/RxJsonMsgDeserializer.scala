package org.bigbluebutton.freeswitch

import org.bigbluebutton.common2.msgs._

import com.fasterxml.jackson.databind.JsonNode

trait RxJsonMsgDeserializer {
  this: RxJsonMsgHdlrActor =>

  object JsonDeserializer extends Deserializer

  def routeGetUsersStatusToVoiceConfSysMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[GetUsersStatusToVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[GetUsersStatusToVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[GetUsersStatusToVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.getUsersStatus(m.body.voiceConf, m.body.meetingId)
    }
  }

  def routeCheckRunningAndRecordingToVoiceConfSysMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[CheckRunningAndRecordingToVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[CheckRunningAndRecordingToVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[CheckRunningAndRecordingToVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.checkRunningAndRecording(m.body.voiceConf, m.body.meetingId)
    }
  }

  def routeGetUsersInVoiceConfSysMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[GetUsersInVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[GetUsersInVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[GetUsersInVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.getAllUsers(m.body.voiceConf)
    }
  }

  def routeEjectAllFromVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[EjectAllFromVoiceConfMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[EjectAllFromVoiceConfMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[EjectAllFromVoiceConfMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.ejectAll(m.body.voiceConf)
    }
  }

  def routeEjectUserFromVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[EjectUserFromVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[EjectUserFromVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[EjectUserFromVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.eject(m.body.voiceConf, m.body.voiceUserId)
    }
  }

  def routeMuteUserInVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[MuteUserInVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[MuteUserInVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[MuteUserInVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.muteUser(m.body.voiceConf, m.body.voiceUserId, m.body.mute)
    }
  }

  def routeTransferUserToVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[TransferUserToVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[TransferUserToVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[TransferUserToVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.transferUserToMeeting(m.body.fromVoiceConf, m.body.toVoiceConf, m.body.voiceUserId)
    }
  }

  def routeStartRecordingVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[StartRecordingVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[StartRecordingVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[StartRecordingVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.startRecording(m.body.voiceConf, m.body.meetingId, m.body.stream)
    }
  }

  def routeStopRecordingVoiceConfMsg(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    def deserialize(jsonNode: JsonNode): Option[StopRecordingVoiceConfSysMsg] = {
      val (result, error) = JsonDeserializer.toBbbCommonMsg[StopRecordingVoiceConfSysMsg](jsonNode)
      result match {
        case Some(msg) => Some(msg.asInstanceOf[StopRecordingVoiceConfSysMsg])
        case None =>
          log.error("Failed to deserialize message: error: {} \n msg: {}", error, jsonNode)
          None
      }
    }

    for {
      m <- deserialize(jsonNode)
    } yield {
      fsApp.stopRecording(m.body.voiceConf, m.body.meetingId, m.body.stream)
    }
  }

}
