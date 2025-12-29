
package dev.streamx.cli.model;

import java.nio.ByteBuffer;

public record Resource(ByteBuffer content, String type) {

}
