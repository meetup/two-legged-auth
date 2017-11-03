package com.meetup.auth.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.ssl.DefaultFactories;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * since Akka was not working with https, this was created to put in a quick-and-dirty http strategy
 * and to test out un-wrapped existing java libs
 */
public class HttpHelper {

    public String test() { return "testing"; }

    //todo not posting POST data, so not using this
    // strategy 2
    public String sendBlockingRequest(String content) throws Exception {
        HttpClientRequest<ByteBuf> request = HttpClientRequest
                .createPost("/oauth2/access")
                .withContent(content)
                ;

        HttpClient<ByteBuf, ByteBuf> rxClient = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder("secure.dev.meetup.com", 443)
                .withSslEngineFactory(DefaultFactories.trustAll())
                .enableWireLogging(LogLevel.DEBUG)
                .build()
                ;
        String o = rxClient.submit(request)
                .flatMap(resp -> resp.getContent()
                    .map(bb -> bb.toString(Charset.defaultCharset()))
                )
                .toBlocking().single()
                ;
        return o;
    }

    /**
     * strategy 3: CURRENTLY IN USE
     * working, but totally not Rx. Janky, but at least it works, jerks!
     * @return entityBody returned as String
     */

    public String sendOldSchoolHttpsPost(String hostAndPath, String contentBody, Map<String,String> headersMap) throws Exception {
        String httpsURL = "https://" + hostAndPath;

        URL myurl = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-Length", String.valueOf(contentBody.length()));
        con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        con.setRequestProperty("User-Agent", "BCAN/0.1");

        for(Map.Entry<String,String> entry : headersMap.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        con.setDoOutput(true);
        con.setDoInput(true);

        DataOutputStream output = new DataOutputStream(con.getOutputStream());


        output.writeBytes(contentBody);

        output.close();

        DataInputStream input = new DataInputStream( con.getInputStream() );

        StringBuilder sb = new StringBuilder();
        for( int c = input.read(); c != -1; c = input.read() ) {
            sb.append((char) c);
        }
        input.close();

        // initial janky debug
        //System.out.println("Resp Code:"+con .getResponseCode());
        //System.out.println("Resp Message:"+ con .getResponseMessage());
        return sb.toString();
    }

}
