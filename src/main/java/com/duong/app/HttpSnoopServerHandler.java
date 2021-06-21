package com.duong.app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;  

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import org.apache.commons.io.IOUtils;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {

	private HttpRequest request;
	/** Buffer that stores the response content */
	private final StringBuilder buf = new StringBuilder();
	private final StringBuilder buf2 = new StringBuilder();
	private String type;
	private byte[] image;
	static private final String Etag= DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;

			if (HttpUtil.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}

			buf.setLength(0);
			buf2.setLength(0);
			if (request.headers().get(HttpHeaderNames.IF_NONE_MATCH) != null) {
				 //System.err.println("checking Etag ...");
				 //System.err.println(HttpSnoopServerHandler.Etag);
				if (request.headers().get(HttpHeaderNames.IF_NONE_MATCH).equals(HttpSnoopServerHandler.Etag)) {
					//System.err.println("not match Etag ...");
					//System.err.println(request.headers().get(HttpHeaderNames.IF_NONE_MATCH)+"=="+HttpSnoopServerHandler.Etag);
					writeResponse304(ctx);
				}
			}
			if (request.uri().equals("/styles")) {
				type = "css";
				//File file = new File(getClass().getClassLoader().getResource("styles.css").getFile());
				InputStream rs = getClass().getClassLoader().getResourceAsStream("styles.css");
				try {
					buf.append(IOUtils.toString(rs, StandardCharsets.UTF_8));
					while (buf.toString().contains("{{uri}}")) {
						buf.replace(buf.indexOf("{{uri}}"), buf.indexOf("{{uri}}") + "{{uri}}".length(),
								request.headers().get(HttpHeaderNames.HOST, "unknown"));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					writeResponseError(ctx, e.getMessage());
				}
			} else if (request.uri().equals("/javascript")) {
				type = "javascript";
				//File file = new File(getClass().getClassLoader().getResource("script.js").getFile());
				InputStream rs2 = getClass().getClassLoader().getResourceAsStream("script.js");
				try {
					buf.append(buf.append(IOUtils.toString(rs2, StandardCharsets.UTF_8)));
					//buf.replace(buf.indexOf("{{uri}}"), buf.indexOf("{{uri}}") + "{{uri}}".length(),
					//		request.headers().get(HttpHeaderNames.HOST, "unknown"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					writeResponseError(ctx, e.getMessage());
				}
			} else if (request.uri().equals("/background")) {
				type = "image";
				// System.err.println("reading file ");
				if (image == null) {
					//File file = new File(getClass().getClassLoader().getResource("background.jpg").getFile());
					InputStream rs3 = getClass().getClassLoader().getResourceAsStream("background.jpg");
					try {
						//image = Files.readAllBytes(file.toPath());
						image=IOUtils.toByteArray(rs3);
					} catch (IOException e) {
						// terminate conection if fail
						writeResponseError(ctx, e.getMessage());
					}
				}
			} else {
				type = "html";
				//File file = new File(getClass().getClassLoader().getResource("index.html").getFile());
				InputStream rs4 = getClass().getClassLoader().getResourceAsStream("index.html");
				try {
					//buf.append(new String(Files.readAllBytes(file.toPath())));
					buf.append(IOUtils.toString(rs4, StandardCharsets.UTF_8));
				} catch (IOException e) {
					// terminate conection if fail
					writeResponseError(ctx, e.getMessage());
				}
				while (buf.toString().contains("{{uri}}")) {
					buf.replace(buf.indexOf("{{uri}}"), buf.indexOf("{{uri}}") + "{{uri}}".length(),
							request.headers().get(HttpHeaderNames.HOST, "unknown"));
				}
				while (buf.toString().contains("{{message}}")) {
				buf.replace(buf.indexOf("{{message}}"), buf.indexOf("{{message}}") + "{{message}}".length(),
						getHello());
				}
				// buf2 is optional
				//buf2.append("p>===================================</p>\r\n");
				if (request.uri().equals("/info")) {
					type = "html info";
					buf2.append("</tr>\r\n");
					buf2.append("<th>TYPE</th>\r\n").append("<th>NAME</th>\r\n").append("<th>VALUE</th>\r\n");
					buf2.append("</tr>\r\n");

					buf2.append("<tr>\r\n<td></td>\r\n");
					buf2.append("<td>VERSION</td>\r\n").append("<td>" + request.protocolVersion() + "</td>\r\n");
					buf2.append("</tr>\r\n");

					buf2.append("<tr>\r\n<td></td>\r\n");
					buf2.append("<td>METHOD</td>\r\n").append("<td>" + request.method().toString() + "</td>\r\n");
					buf2.append("</tr>\r\n");

					buf2.append("<tr>\r\n<td></td>\r\n");
					buf2.append("<td>HOSTNAME</td>\r\n")
							.append("<td>" + request.headers().get(HttpHeaderNames.HOST, "unknown") + "</td>\r\n");

					buf2.append("<tr>\r\n<td></td>\r\n");
					buf2.append("<td>REQUEST_URI</td>\r\n").append("<td>" + request.uri() + "</td>\r\n");
					buf2.append("</tr>\r\n");

					HttpHeaders headers = request.headers();
					if (!headers.isEmpty()) {
						for (Map.Entry<String, String> h : headers) {
							CharSequence key = h.getKey();
							CharSequence value = h.getValue();
							buf2.append("<tr>\r\n");
							buf2.append("<td>HEADER</td>\r\n").append("<td>" + key + "</td>\r\n")
									.append("<td>" + value + "</td>\r\n");
							buf2.append("</tr>\r\n");
						}
						buf2.append("\r\n");
					}

					QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
					Map<String, List<String>> params = queryStringDecoder.parameters();
					if (!params.isEmpty()) {
						for (Entry<String, List<String>> p : params.entrySet()) {
							String key = p.getKey();
							List<String> vals = p.getValue();
							for (String val : vals) {
								buf2.append("<tr>\r\n");
								buf2.append("<td>PARAM</td>\r\n").append("<td>" + key + "</td>\r\n")
										.append("<td>" + val + "</td>\r\n");
								buf2.append("</tr>\r\n");
							}
						}
						buf2.append("\r\n");
					}
					appendDecoderResult(buf2, request);
				}
			}
		}
		if (msg instanceof HttpContent)

		{
			// thuc thi neu là get html
			if (type.equals("html info")) {
				HttpContent httpContent = (HttpContent) msg;
				ByteBuf content = httpContent.content();
				if (content.isReadable()) {
					buf2.append("<tr>\r\n<td></td>\r\n");
					buf2.append("<td>CONTENT</td>\r\n");
					buf2.append("<td>"+content.toString(CharsetUtil.UTF_8)+"</td>\r\n");
					buf2.append("</tr>\r\n");
					appendDecoderResult(buf2, request);
				}
			}
			if (msg instanceof LastHttpContent) {
				LastHttpContent trailer = (LastHttpContent) msg;
				// thuc thi neu là get html
				if (type.equals("html info")) {
					//buf2.append("<p>END OF CONTENT</p>\r\n");
					if (!trailer.trailingHeaders().isEmpty()) {
						for (CharSequence name : trailer.trailingHeaders().names()) {
							for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
								buf2.append("<tr>\r\n");
								buf2.append("<td>TRAILING HEADER</td>\r\n");
								buf2.append("<td>"+name+"</td>\r\n").append("<td>"+value+"</td>\r\n");
								buf2.append("</tr>\r\n");
							}
						}

					}
					// insert buf2 to buf
					// buf.insert(buf.indexOf("</body>"), buf2.toString());
					//buf.replace(buf.indexOf("</body>"), buf.indexOf("</body>") + "</body>".length(),
					//		buf2.toString() + "</body>");
					
					buf.replace(buf.indexOf("<!--{{HTTP info}}-->"), buf.indexOf("<!--{{HTTP info}}-->") + "<!--{{HTTP info}}-->".length(),
							buf2.toString());
				}
				if (!writeResponse(trailer, ctx, type)) {
					// If keep-alive is off, close the connection once the content is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}

	}

	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.decoderResult();
		if (result.isSuccess()) {
			return;
		}

		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	public static String getHello() {
		return "Hello world !";
	}

	// private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext
	// ctx) {
	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx, String type) {
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpUtil.isKeepAlive(request);

		// response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;
		// charset=UTF-8");
		if (type.equals("css") || type.equals("html") ||  type.equals("html info") || type.equals("javascript")) {
			// Build the response object.
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
					Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
			if (type.equals("css") || type.equals("html") || type.equals("javascript"))
			{
				response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public");
				response.headers().set(HttpHeaderNames.CACHE_CONTROL, "must-revalidate");
				response.headers().set(HttpHeaderNames.ETAG, HttpSnoopServerHandler.Etag);
				
			}
			if (type.equals("css")) {
				response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
				//response.headers().set(HttpHeaderNames.CONTENT_LOCATION, "/styles");
			} else if (type.equals("javascript")) {
				response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/javascript; charset=UTF-8");
				//response.headers().set(HttpHeaderNames.CONTENT_LOCATION, "/javascript");
			} else if (type.equals("html")|| type.equals("html info")) {
				response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
				/*
				if (type.equals("html")) {
					response.headers().set(HttpHeaderNames.CONTENT_LOCATION, "/");
				} else if (type.equals("html info")) {
					response.headers().set(HttpHeaderNames.CONTENT_LOCATION, "/info");
				}*/
				String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
				if (cookieString != null) {
					Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
					if (!cookies.isEmpty()) {
						// Reset the cookies if necessary.
						/*
						 * for (Cookie cookie : cookies) {
						 * response.headers().add(HttpHeaderNames.SET_COOKIE,
						 * ServerCookieEncoder.STRICT.encode(cookie)); }
						 */
						// System.err.println("delete cookie");
						// response.headers().add(HttpHeaderNames.SET_COOKIE,
						// ServerCookieEncoder.STRICT.encode("Max-Age", "0"));
					}
				} else {
					// Browser sent no cookie. Add some.
					// System.err.println("get cookie");
					response.headers().add(HttpHeaderNames.SET_COOKIE,
							ServerCookieEncoder.STRICT.encode("key1", "value1"));
					response.headers().add(HttpHeaderNames.SET_COOKIE,
							ServerCookieEncoder.STRICT.encode("key2", "value2"));
				}

			}
			if (keepAlive) {
				// Add 'Content-Length' header only for a keep-alive connection.
				response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
				// Add keep alive header as per:
				// -
				// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}

			// Encode the cookie.

			ctx.write(response);

		} else if (type.equals("image")) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(image));
			response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
			response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public");
			response.headers().set(HttpHeaderNames.CACHE_CONTROL, "must-revalidate");
			response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=5000000");
			response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-stale=0");
			response.headers().set(HttpHeaderNames.ETAG, HttpSnoopServerHandler.Etag);
			//response.headers().set(HttpHeaderNames.CONTENT_LOCATION, "/background");
			if (keepAlive) {
				response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				
			}
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/jpg");
			ctx.write(response);
		}
		return keepAlive;
		// return false;
	}

	private void writeResponseError(ChannelHandlerContext ctx, String msg) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND,
				Unpooled.copiedBuffer("could not load resource: \r\n"+msg, CharsetUtil.UTF_8));
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	private void writeResponse304(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED,
				Unpooled.copiedBuffer("resource not modified", CharsetUtil.UTF_8));
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		response.headers().set(HttpHeaderNames.ETAG, HttpSnoopServerHandler.Etag);
	}
	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
		ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
