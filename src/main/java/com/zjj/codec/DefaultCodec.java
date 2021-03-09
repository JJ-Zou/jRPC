package com.zjj.codec;

import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.exception.AbstractJRpcException;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.protocol.ProtocolVersion;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.rpc.message.DefaultResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultCodec extends AbstractCodec {

    @Override
    public byte[] encode(Object message) throws IOException {
        if (message instanceof Request) {
            return encodeRequest((Request) message);
        }
        if (message instanceof Response) {
            return encodeResponse((Response) message);
        }
        throw new JRpcFrameworkException("encode " + message + " error, message type " + message.getClass() + " not supported.",
                JRpcErrorMessage.FRAMEWORK_ENCODE_ERROR);
    }

    private byte[] encodeRequest(Request request) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutput objectOutput = new ObjectOutputStream(outputStream)) {
            objectOutput.writeUTF(request.getInterfaceName());
            objectOutput.writeUTF(request.getMethodName());
            objectOutput.writeUTF(request.getParameterSign());
            Arrays.stream(request.getArguments()).forEach(o -> {
                try {
                    objectOutput.writeObject(serialization.serialize(o));
                } catch (IOException e) {
                    log.error("DefaultCodec encode Request {} error when write argument {}", request, o);
                }
            });
            Map<String, String> attachments = request.getAttachments();
            objectOutput.writeInt(attachments.size());
            attachments.forEach((k, v) -> {
                try {
                    objectOutput.writeUTF(k);
                    objectOutput.writeUTF(v);
                } catch (IOException e) {
                    log.error("DefaultCodec encode Request {} error when writeUTF (key = {}, value = {})", request, k, v);
                }
            });
            objectOutput.flush();
            byte[] body = outputStream.toByteArray();
            byte flag = JRpcURLParamType.requestFlag.getByteValue();
            return encode(body, flag, request.getRequestId());
        }
    }

    private byte[] encodeResponse(Response response) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutput objectOutput = new ObjectOutputStream(outputStream)) {
            objectOutput.writeLong(response.getProcessTime());
            byte flag;
            Exception exception = response.getException();
            if (exception != null) {
                flag = JRpcURLParamType.responseException.getByteValue();
                objectOutput.writeUTF(exception.getClass().getName());
                objectOutput.writeObject(serialization.serialize(exception));
            } else {
                Object value = response.getValue();
                if (value == null) {
                    flag = JRpcURLParamType.responseVoid.getByteValue();
                } else {
                    flag = JRpcURLParamType.responseFlag.getByteValue();
                    objectOutput.writeUTF(value.getClass().getName());
                    objectOutput.writeObject(serialization.serialize(value));
                }
            }
            objectOutput.flush();
            byte[] body = outputStream.toByteArray();
            return encode(body, flag, response.getRequestId());
        }
    }

    private byte[] encode(byte[] body, byte flag, long requestId) {
        int headLen = ProtocolVersion.DEFAULT_VERSION.getHeadLength();
        int bodyLen = body.length;
        byte[] data = new byte[headLen + bodyLen];
        int offset = 0;
        short magic = JRpcURLParamType.magicNum.getShortValue();
        data[offset++] = (byte) (magic >> 8);
        data[offset++] = (byte) magic;
        byte version = ProtocolVersion.DEFAULT_VERSION.getVersion();
        data[offset++] = version;
        data[offset++] = flag;
        data[offset++] = (byte) (requestId >> 56);
        data[offset++] = (byte) (requestId >> 48);
        data[offset++] = (byte) (requestId >> 40);
        data[offset++] = (byte) (requestId >> 32);
        data[offset++] = (byte) (requestId >> 24);
        data[offset++] = (byte) (requestId >> 16);
        data[offset++] = (byte) (requestId >> 8);
        data[offset++] = (byte) requestId;
        data[offset++] = (byte) (bodyLen >> 24);
        data[offset++] = (byte) (bodyLen >> 16);
        data[offset++] = (byte) (bodyLen >> 8);
        data[offset++] = (byte) bodyLen;
        System.arraycopy(body, 0, data, offset, bodyLen);
        return data;
    }


    @Override
    public Object decode(byte[] bytes) throws IOException {
        int headLen = ProtocolVersion.DEFAULT_VERSION.getHeadLength();
        if (bytes.length <= headLen) {
            throw new JRpcFrameworkException("DefaultCodec decode error, lack length.", JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
        }
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        short magic = byteBuf.readShort();
        if (magic != JRpcURLParamType.magicNum.getShortValue()) {
            throw new JRpcFrameworkException("DefaultCodec decode error, magic error.", JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
        }
        byte version = byteBuf.readByte();
        if (version != ProtocolVersion.DEFAULT_VERSION.getVersion()) {
            throw new JRpcFrameworkException("DefaultCodec decode error, version error.", JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
        }
        byte flag = byteBuf.readByte();
        boolean isRequest = flag == JRpcURLParamType.requestFlag.getByteValue();
        long requestId = byteBuf.readLong();
        int bodyLen = byteBuf.readInt();
        if (bodyLen + headLen != bytes.length) {
            throw new JRpcFrameworkException("DefaultCodec decode error, content length error.", JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
        }
        byte[] body = new byte[bodyLen];
        byteBuf.readBytes(body);
        try {
            if (isRequest) {
                return decodeRequest(body, requestId);
            } else {
                return decodeResponse(body, requestId, flag);
            }
        } catch (Exception e) {
            if (AbstractJRpcException.class.isAssignableFrom(e.getClass())) {
                throw new JRpcFrameworkException("DefaultCodec decode error, " + (isRequest ? "request" : "response") + " body error.", e, JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
            } else {
                log.error("DefaultCodec decode error.", e);
                throw e;
            }
        }
    }

    private Object decodeRequest(byte[] body, long requestId) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
             ObjectInput objectInput = new ObjectInputStream(inputStream)) {
            String interfaceName = objectInput.readUTF();
            String methodName = objectInput.readUTF();
            String parameterSign = objectInput.readUTF();
            List<Class<?>> classes = ReflectUtils.fromClassNames(parameterSign);
            Object[] arguments = classes.stream().map(clazz -> {
                Object object = null;
                try {
                    object = objectInput.readObject();
                    return serialization.deserialize((byte[]) object, clazz);
                } catch (ClassNotFoundException | IOException e) {
                    log.error("{} deserialize {} error", serialization, object, e);
                    return null;
                }
            }).toArray(Object[]::new);
            Map<String, String> map = new HashMap<>();
            int size = objectInput.readInt();
            for (int i = 0; i < size; i++) {
                map.put(objectInput.readUTF(), objectInput.readUTF());
            }
            return DefaultRequest.builder()
                    .requestId(requestId)
                    .interfaceName(interfaceName)
                    .methodName(methodName)
                    .parameterSign(parameterSign)
                    .arguments(arguments)
                    .attachments(map)
                    .build();
        }
    }

    private Object decodeResponse(byte[] body, long requestId, byte flag) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
             ObjectInput objectInput = new ObjectInputStream(inputStream)) {
            long processTime = objectInput.readLong();
            DefaultResponse.DefaultResponseBuilder builder = DefaultResponse.builder()
                    .processTime(processTime)
                    .requestId(requestId);
            if (flag == JRpcURLParamType.responseVoid.getByteValue()) {
                return builder.build();
            }
            String className = objectInput.readUTF();
            Class<?> clazz = ReflectUtils.fromClassName(className);
            Object result;
            try {
                result = serialization.deserialize((byte[]) objectInput.readObject(), clazz);
            } catch (ClassNotFoundException e) {
                log.error("DefaultCodec decode {} error.", clazz);
                result = null;
            }
            if (flag == JRpcURLParamType.responseException.getByteValue()) {
                builder.exception((Exception) result);
            } else if (flag == JRpcURLParamType.responseFlag.getByteValue()) {
                builder.value(result);
            } else {
                log.error("DefaultCodec decode class {} instance {} error: unsupported response type {}", clazz, result, flag);
                throw new JRpcFrameworkException("DefaultCodec decode error", JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
            }
            return builder.build();
        }
    }
}
