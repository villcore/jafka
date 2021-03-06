/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jafka.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Convert ByteBuffer to InputStream
 * @author adyliu (imxylz@gmail.com)
 * @since 1.0
 */
class ByteBufferBackedInputStream extends InputStream {

    private final ByteBuffer buffer;

    public ByteBufferBackedInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        return buffer.hasRemaining() ? (buffer.get() & 0xFF) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (buffer.hasRemaining()) {
            int realLen = Math.min(len, buffer.remaining());
            buffer.get(b, off, realLen);
            return realLen;
        }
        return -1;
    }

}
