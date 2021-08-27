package org.bigbluebutton.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

@Validated
@Data
public class InlineResponse200 {

  @JsonProperty("payload")
  private InlineResponse200Payload payload = null;

  @JsonProperty("errors")
  private Object errors = null;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200 inlineResponse200 = (InlineResponse200) o;
    return Objects.equals(this.payload, inlineResponse200.payload) &&
        Objects.equals(this.errors, inlineResponse200.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payload, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200 {\n");
    
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
