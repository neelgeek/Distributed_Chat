package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.Message;
import org.json.JSONObject;

/**
 * This class handles the encapsulation of request and response payloads and also encoding and decoding them into
 * JSON formats.
 */
public class ProtocolHandler {

  /**
   * Converts the request payload to JSON format.
   *
   * @param payload Payload to be encoded
   * @return JSON object converted to string format
   */
  public static String encodeJSONRequest(Message payload) {
    JSONObject encodedPayload = new JSONObject(payload);
    return encodedPayload.toString();
  }

  /**
   * Converts the response payload to JSON format.
   *
   * @param payload Payload to be encoded
   * @return JSON object converted to string format
   */
  public static String encodeJSONResponse(Message payload) {
    JSONObject encodedPayload = new JSONObject(payload);
    return encodedPayload.toString();
  }

  /**
   * Converts the response from the server to {@link ResponsePayload} object
   *
   * @param encodedPayload Response from the server in String format
   * @param pojo
   * @return Response parsed to {@link ResponsePayload} format
   */
  public static Message decodeJSONResponse(String encodedPayload) {
    try {
      Message javaObject = new Gson().fromJson(encodedPayload, Message.class);
      return javaObject;
    } catch (JsonSyntaxException e) {

    }
    return null;
  }

  /**
   * Converts the request from the client to {@link RequestPayload} object
   *
   * @param encodedPayload Request from the client in String format
   * @return Request parsed to {@link RequestPayload} format
   */
  public static Message decodeJSONRequest(String encodedPayload) {
    try {
      Message payload = new Gson().fromJson(encodedPayload, Message.class);
      return payload;
    } catch (JsonSyntaxException e) {

    }
    return null;
  }

}

