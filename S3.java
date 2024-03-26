/* 
 *  CS 656 Fall 2023 Project: AWS S3 buckets V3.30
 *  Copyright (C) New Jersey Institute of Technology
 *  All rights reserved
 *
 *
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;
/* -- end of imports -- */

class S3 {
    private byte []     HOST;
    private int         PORT;
    private InetAddress Addr;
    
    static byte OkHeader[]={72,84,84,80,47,49,46,49,32,50,48,48,32,79,75,13,10,67,111,110,116,101,110,116,45,84,
    121,112,101,58,32,116,101,120,116,47,112,108,97,105,110,13,10,13,10};//45
    static byte BadRequestHeader[]={72,84,84,80,47,49,46,49,32,52,48,49,32,66,97,100,32,82,101,113,117,101,115,
    116,13,10,67,111,110,116,101,110,116,45,84,121,112,101,58,32,116,101,120,116,47,112,108,97,105,110,13,10,13,10}; //54
    static byte NotFoundHeader[]={72,84,84,80,47,49,46,49,32,52,48,52,32,78,111,116,32,70,111,117,110,100,13,10,67,
    111,110,116,101,110,116,45,84,121,112,101,58,32,116,101,120,116,47,112,108,97,105,110,13,10,13,10};
    static byte InternalServerErrorHeader[]={72,84,84,80,47,49,46,49,32,53,48,48,32,73,110,116,101,114,
    110,97,108,32,83,101,114,118,101,114,32,69,114,114,111,114,13,10};
    static byte localReqCheck[]={69,84,32,47,71,69,84,47,102,120,58,47,47};
    static byte remoteReqCheck[]={69,84,32,47,71,69,84,47,104,120,58,47,47};
    static byte favicon[]={69,84,32,47,102,97,118,105,99,111,110,46,105};                
    static byte BadRequestResponse[]={66,65,68,32,82,69,81,85,69,83,84};
    static byte IncorrectResponse[]={73,110,99,111,114,114,101,99,116,32,86,97,108,117,101,115,32,40,79,102,
    102,115,101,116,32,38,32,76,101,110,103,116,104,41};//34
    static byte NotFoundResponse[]={10,70,105,108,101,32,78,111,116,32,70,111,117,110,100,32,105,110,32,116,
    104,101,32,83,101,114,118,101,114};
    static byte InternalServerErrorResponse[]={73,110,116,101,114,110,97,108,32,83,101,114,118,101,114,32,
    69,114,114,111,114};
    static byte UnreachableURLResponse[]={67,111,117,108,100,32,110,111,116,32,99,111,110,110,101,99,116,32,
    116,111,32,116,104,101,32,103,105,118,101,110,32,85,82,76,46,32,75,105,110,100,108,121,32,99,104,101,99,
    107,32,116,104,101,32,85,82,76,32,97,110,100,32,116,114,121,32,97,103,97,105,110,46};
    static byte EndOfFileResponse[]={45,45,45,45,45,45,45,69,110,100,32,111,102,32,102,105,108,101,32,114,101,
    97,99,104,101,100,45,45,45,45,45,45,45};

    static byte[] oxString ;
    static byte[] lxString ;
    static byte[] fileString;
    static int readMode=0;
    static int flag=0;
    static int fileNameLength=0;
    static int offsetNumbLength=0;
    static int lengthNumbLength=0;
    static int isHXMODE=0,remoteUrlEndIndex=0,remoteUrlStartIndex=0;    
    static byte[] remoteUrl;
    
    public static void main(String [] a) {   
        
    S3 bucket = new S3(Integer.parseInt(a[0]));
        
        bucket.run(0);
    }

    public S3 (int port) { PORT = port; }           

    int parse(byte [] buf) {                           
        return 0;
    }

    int dns(int X) {                               
        return 0;                                  
    }
    
    int run(int X)                         
    {   
        try(
            ServerSocket s0 = new ServerSocket(PORT);  
        ){
            while(true){
                Socket s1 = s0.accept();
                InputStream s1InputStream = s1.getInputStream();
                OutputStream s1OutputStream = s1.getOutputStream();
                byte[] b0=null;
                
                byte l[]=new byte[1];
                s1InputStream.read(l,0,1);
                l=null;
                
                int inputStreamLength=s1InputStream.available();

                b0=new byte[inputStreamLength];
                s1InputStream.read(b0,0,inputStreamLength);
                
                for(int i=4;i<13;i++){
                    if(b0[i]!=localReqCheck[i]){ 
                        if(b0[i]==remoteReqCheck[i]){
                        }
                        else if(b0[i]==favicon[i]){
                            flag=2;
                            break;
                        }else{
                            flag=1;
                            break;
                        }
                    } 
                }

                if(flag==0){
                    readUrlParams(b0);                    
                    if(flag==1){
                        s1OutputStream.write(BadRequestHeader,0,54);
                        s1OutputStream.write(BadRequestResponse,0,11);
                    }
                    else{
                        if(isHXMODE==0){
                            read(fileString,byte2int(oxString,offsetNumbLength),byte2int(lxString,lengthNumbLength),s1OutputStream);
                        }else if(isHXMODE==1){                            
                            byte b0_sub[]=new byte[remoteUrlEndIndex-remoteUrlStartIndex+1];
                            for(int i=0,j=remoteUrlStartIndex+i;i<(remoteUrlEndIndex-remoteUrlStartIndex);i++,j++){
                                b0_sub[i]=b0[j];
                            }

                            getRemoteUrl(b0_sub);
                            if(flag==1)
                            {
                                s1OutputStream.write(BadRequestHeader,0,54);
                                s1OutputStream.write(BadRequestResponse,0,11);
                            }else{
                                remoteRead(remoteUrl, byte2int(oxString,offsetNumbLength), byte2int(lxString,lengthNumbLength), s1OutputStream);                            
                            }
                        }
                    }
                }
                else if(flag==1)
                {
                    s1OutputStream.write(BadRequestHeader,0,54);
                    s1OutputStream.write(BadRequestResponse,0,11);
                }
                else if(flag==2)
                {
                    s1OutputStream.write(NotFoundHeader,0,NotFoundHeader.length);
                }
                
                s1InputStream.close();
                s1OutputStream.close();
                s1.close();

                oxString=null;
                lxString=null;
                fileString=null;
                readMode=0;
                flag=0;
                fileNameLength=0;
                offsetNumbLength=0;
                lengthNumbLength=0;
                isHXMODE=0;
                remoteUrlStartIndex=0;
                remoteUrlEndIndex=0;
            }
        } 
        catch(Exception e){
        }
        return 0;
    } /* run */

    /* ------------- your own methods below this line ONLY ----- */

    static int byte2int(byte[] b, int j){
        int number=0;
        for(int i=0;i<=j;i++){
            if(b[i]<48){
                number=-23;
            }else if(b[i]>57){
                number=-23;
            }
            else{
                number*=10;
                number+=b[i]%48;
            }
        }
        return number;
    }
    
    static String byte2str(byte[] b, int i, int j) {
        byte [] b2 = new byte [j-i+1];
        int temp=i;
        for(int k=0;k<=(j-i);k++,temp++){
            if(b[temp]!=0){
                b2[k]=b[temp];   
            }   
        }
        return new String( b2 );
    }
    
    private int readUrlParams(byte[] buf)
    {
        int oxPointer = 0,lxPointer = 0,filePointer = 0;
        
            for (int i = 0; i < buf.length; i++) {
                if(buf[i]==10)
                {
                    remoteUrlEndIndex=i-1;
                    break;
                }

                if (buf[i] ==37 ) {
                    if (buf[i+1] == 50) {
                        if (buf[i+2] == 48) {
                            flag=1;
                            return 0;
                        }
                    }
                }
                else if (buf[i] == 38 && buf[i + 1] == 111 ) {
                    if(buf[i+2]==120){
                        if(buf[i+3]==61){
                            oxPointer = i + 4;
                        }else{
                            flag=1;
                            return 0;
                        }
                    }else{
                        flag=1;
                        return 0;
                    }
                }
                else if (buf[i] == 38 && buf[i + 1] == 108) {
                    if(buf[i+2]==120){
                        if(buf[i+3]==61){
                            lxPointer = i + 4;
                        }else{
                            flag=1;
                            return 0;
                        }
                    }else{
                        flag=1;
                        return 0;
                    }
                }
                else if (buf[i] == 102 && buf[i+1] == 120 && buf[i + 2] == 58 && buf[i+3] == 47 && buf[i + 4] == 47 ) {
                    filePointer = i + 5;
                }
                else if(buf[i] == 104 && buf[i+1] == 120 && buf[i + 2] == 58 && buf[i+3] == 47 && buf[i + 4] == 47 ){
                    filePointer=i+5;
                    isHXMODE=1;
                    remoteUrlStartIndex=i+5;
                }
            }
            
            if (buf[filePointer] == 38 || buf[filePointer] == 32) {
                flag=1;
                return 0;
            }
            
            if (oxPointer == 0 && lxPointer == 0) {
                int i=0;
                
                for (int j = filePointer; j < buf.length && buf[j] != 32; j++) {
                    i++;
                }
                fileString=new byte[i];
                fileNameLength=i-1;
                i=0;
                for (int j = filePointer; j < buf.length && buf[j] != 32; j++) {
                    fileString[i++] = buf[j];
                }
                readMode=1;

                oxString=new byte[1];
                lxString=new byte[1];
                return 1;
            }

            if (oxPointer != 0 && lxPointer == 0) {
                int i=0;

                for (int j = filePointer; j < buf.length && buf[j] != 38; j++) {
                    i++;
                }
                fileString=new byte[i];
                fileNameLength=i-1;
                i=0;
                for (int j = filePointer; j < buf.length && buf[j] != 38; j++) {
                    fileString[i++] = buf[j];
                }
                
                i = 0;
                for (int k = oxPointer; k < buf.length && buf[k] != 32; k++) {
                    i++;
                }
                oxString=new byte[i];
                offsetNumbLength=i-1;
                i=0;
                for (int k = oxPointer; k < buf.length && buf[k] != 32; k++) {
                    oxString[i++] = buf[k];
                }
                lxString=new byte[1];
                readMode=2;
                return 1;
            }
            
            int i = 0;
            for (int j = filePointer; j < buf.length && buf[j] != 38; j++) {
                i++;
            }
            fileString=new byte[i];
            fileNameLength=i-1;
            i=0;
            for (int j = filePointer; j < buf.length && buf[j] != 38; j++) {
                fileString[i++] = buf[j];
            }

            i = 0;
            for (int k = oxPointer; k < buf.length && buf[k] != 38; k++) {
                i++;
            }
            oxString=new byte[i];
            offsetNumbLength=i-1;
            i=0;
            for (int k = oxPointer; k < buf.length && buf[k] != 38; k++) {
                oxString[i++] = buf[k];
            }

            i = 0;
            for (int l = lxPointer; l < buf.length && buf[l] != 32; l++) {
                i++;
                
            }
            lxString=new byte[i];
            lengthNumbLength=i-1;
            i=0;
            for (int l = lxPointer; l < buf.length && buf[l] != 32; l++) {
                lxString[i++] = buf[l];   
            }
            readMode=3;
            return 1;

               
    }

    private static int read(byte[] filename,int offsetReq,int lengthReq,OutputStream outputStream){
        
        try{            
        int totalLengthFile;
        File myfile=new File(byte2str(filename, 0, fileNameLength));
        try{
            FileInputStream FileIO=new FileInputStream(myfile);          
            totalLengthFile=FileIO.available();

            if(readMode==1){
                lengthReq=totalLengthFile;
                offsetReq=0;
            }else if(readMode==2){
                lengthReq=totalLengthFile;
                if(offsetReq<0){
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                    FileIO.close();
                    return 0;
                }   
            }else if(readMode==3){
                if(offsetReq<0||lengthReq<0){
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                    FileIO.close();
                    return 0;
                }   
            }   
            
            if(offsetReq>=totalLengthFile||lengthReq>totalLengthFile){
                outputStream.write(BadRequestHeader,0,BadRequestHeader.length);    
                outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                FileIO.close();
                return 0;
            }else{
                FileIO.skip(offsetReq);
                
                int iterations=0;
                int bufferSize=1500;//Buffer Size(Variable) Can be changed. Default set to TCP standard packet size
                if(lengthReq<bufferSize){
                    bufferSize=lengthReq;
                }else if(lengthReq>bufferSize){
                    iterations=(lengthReq/bufferSize);
                }

                byte[] fileBuffer=new byte[bufferSize];
                outputStream.write(OkHeader,0,45);

                for(int k=0;k<=iterations;k++){
                    if(k==iterations){
                        bufferSize=lengthReq;
                    }
                    
                    FileIO.read(fileBuffer,0,bufferSize);
                    outputStream.write(fileBuffer,0,bufferSize);
                    
                    lengthReq-=bufferSize;
                    
                }
                fileBuffer=null;
            }
            FileIO.close();
        }catch(Exception e){
            outputStream.write(NotFoundHeader,0,NotFoundHeader.length);
            outputStream.write(NotFoundResponse,0,NotFoundResponse.length);
        }
        }catch(Exception e){
        }
        return 1;
    }

    private static void getRemoteUrl(byte[] url){
        try{
            int i=0;
            int protocolPointer=0;
            
            try{
                if(url[i]==104&&url[i+1]==116&&url[i+2]==116&&url[i+3]==112){
                    if(url[i+4]==58){
                        if(url[i+5]==47&&url[i+6]==47){
                            if(url[i+7]==119&&url[i+8]==119&&url[i+9]==119&&url[i+10]==46){
                                i+=11;
                            }
                            else{
                                i+=7;
                            }
                            protocolPointer=i;
                        }else{
                            flag=1;
                        }
                    }
                    else{
                        flag=1;
                    }
                }else{
                    flag=1;
                }
                
                if(flag==0){
                    int pathends=0;
                    for(int k=protocolPointer;k<url.length;k++){
                        if(url[k]==38 &&url[k+1]==111&&url[k+2]==120){
                            pathends=k;
                            break;
                        }else if(url[k+1]==32){
                            pathends=k+1;
                            break;
                        }
                    
                    }
                    remoteUrl=new byte[pathends-protocolPointer];
                    for(int k=protocolPointer,remoteUrlindex=0;k<pathends;k++,remoteUrlindex++){
                        remoteUrl[remoteUrlindex]=url[k];
                    }
                    
                }
                
            }catch(ArrayIndexOutOfBoundsException e){
                return;
            }
            }catch(ArrayIndexOutOfBoundsException e){
                return;
            }
    }
    
    private static void remoteRead(byte[] url, int offsetReq, int lengthReq, OutputStream outputStream)
    {
        try{
            if(readMode==1){    
                offsetReq=0;
            }else if(readMode==2){
                if(offsetReq<0){
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                    return;
                }   
            }else if(readMode==3){
                if(offsetReq<0||lengthReq<0){
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                    return;
                }   
            }

            int i=0;
            while(url[i] != '/')
            {
                i++;
            }

            byte domain[] = new byte[i];
            byte path[] = new byte[url.length-i];

            for(int j=0;j<url.length;j++)
            {
                if(j<i)
                {
                    domain[j] = url[j];
                }
                else
                {
                    path[j-i] = url[j];
                }
            }
               
            byte[] protocol = {32,72,84,84,80,47,49,46,48,13,10};
            byte[] requestHeader = {67,111,110,110,101,99,116,105,111,110,58,32,99,108,111,115,101,13,10,13,10};
            InetAddress remoteAddress = getDomainAddress(domain);
            if(remoteAddress == null)
            {
                outputStream.write(BadRequestHeader, 0, BadRequestHeader.length);
                outputStream.write(UnreachableURLResponse, 0, UnreachableURLResponse.length);
                return;
            }

            try (
                Socket httpGetSocket = new Socket(remoteAddress,80);
                InputStream getInputStream = httpGetSocket.getInputStream();
                OutputStream getOutputStream = httpGetSocket.getOutputStream();
            ) {
                byte[] getMethod = {71,69,84,32};
                byte[] httpGetRequest = new byte[getMethod.length+path.length+protocol.length+requestHeader.length];
                
                int k=0;
                for(int j=0;j<getMethod.length;j++)
                {
                    httpGetRequest[k] = getMethod[j];
                    k++;
                }
                for(int j=0;j<path.length;j++)
                {
                    httpGetRequest[k] = path[j];
                    k++;
                }
                for(int j=0;j<protocol.length;j++)
                {
                    httpGetRequest[k] = protocol[j];
                    k++;
                }
                for(int j=0;j<requestHeader.length;j++)
                {
                    httpGetRequest[k] = requestHeader[j];
                    k++;
                }

                getOutputStream.write(httpGetRequest);
                
                byte[] httpGetResponse = new byte[1500]; 
                int bytesRead = getInputStream.read(httpGetResponse,0, 1500);

                int pointer=0;
                while(httpGetResponse[pointer] != 32)
                {
                    pointer++;
                }
                pointer++; 

                byte[] responseHeaderCode = new byte[3]; 
                for(int j=0;j<3;j++)
                {
                    responseHeaderCode[j] = httpGetResponse[pointer];
                    pointer++;
                }

                int responseStatusCode = byte2int(responseHeaderCode, 2);

                if (responseStatusCode == 400) {
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(BadRequestResponse,0,BadRequestResponse.length);
                    return;
                }
                else if(responseStatusCode == 404)
                {
                    outputStream.write(NotFoundHeader,0,NotFoundHeader.length);
                    outputStream.write(NotFoundResponse,0,NotFoundResponse.length);
                    return;
                }
                else if(responseStatusCode == 500)
                {
                    outputStream.write(InternalServerErrorHeader,0,InternalServerErrorHeader.length);
                    outputStream.write(InternalServerErrorResponse,0,InternalServerErrorResponse.length);
                    return;
                }
                else if(responseStatusCode != 200)
                {
                    outputStream.write(BadRequestHeader, 0, BadRequestHeader.length);
                    outputStream.write(BadRequestResponse, 0, BadRequestResponse.length);
                    return;
                }

                while(httpGetResponse[pointer] != 13 || httpGetResponse[pointer+1] != 10 || httpGetResponse[pointer+2] != 13 || httpGetResponse[pointer+3] != 10){
                    pointer++;
                }
                pointer+=4;
                bytesRead-=pointer;

                if(offsetReq >= bytesRead)
                {
                    getInputStream.skip(offsetReq-bytesRead);
                    if(getInputStream.available() == 0)
                    {
                        outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                        outputStream.write(IncorrectResponse,0,IncorrectResponse.length);
                        return;
                    } 
                }

                if(readMode==1 || readMode == 2) 
                {
                    outputStream.write(OkHeader,0,OkHeader.length);

                    if(offsetReq < bytesRead)
                    {
                        outputStream.write(httpGetResponse,pointer+offsetReq,bytesRead-offsetReq);
                    }

                    bytesRead = getInputStream.read(httpGetResponse,0, 1500);
                    while(bytesRead != -1) 
                    {
                        outputStream.write(httpGetResponse,0,bytesRead);
                        bytesRead = getInputStream.read(httpGetResponse,0, 1500);
                    }
                }
                else if(readMode==3) 
                {
                    outputStream.write(OkHeader,0,OkHeader.length);

                    if(offsetReq < bytesRead)
                    {
                        if(offsetReq+lengthReq <= bytesRead)
                        {
                            outputStream.write(httpGetResponse,pointer+offsetReq,lengthReq);
                            return;
                        }
                        else
                        {
                            outputStream.write(httpGetResponse,pointer+offsetReq,bytesRead-offsetReq);
                            lengthReq = lengthReq - (bytesRead-offsetReq);
                        }
                    }

                    bytesRead = getInputStream.read(httpGetResponse,0, 1500);
                    pointer = 0;
                    int lastPointer = bytesRead-1;
                    int lengthToRead = lengthReq;

                    while(pointer+lengthToRead>lastPointer && bytesRead != -1)
                    {
                        outputStream.write(httpGetResponse,pointer%bytesRead,bytesRead-(pointer%bytesRead));
                        lengthToRead = lengthToRead-(lastPointer-pointer);  
                        bytesRead = getInputStream.read(httpGetResponse,0, 1500);
                        pointer = lastPointer+1; 
                        lastPointer = lastPointer+bytesRead;   
                    }

                    if(bytesRead != -1) 
                    {
                        outputStream.write(httpGetResponse,pointer%bytesRead,lengthToRead);
                    }
            }
            } catch (Exception e) {
                try {
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(BadRequestResponse,0,BadRequestResponse.length);
                } catch (Exception e1) {
                    return;
                }
            }   
        }
        catch(Exception e)
        {
            try {
                    outputStream.write(BadRequestHeader,0,BadRequestHeader.length);
                    outputStream.write(BadRequestResponse,0,BadRequestResponse.length);
                } catch (Exception e1) {
                    return;
                }
        }
    }

    private static InetAddress getDomainAddress(byte[] domain)
    {   
        try {
            InetAddress address = InetAddress.getByName(byte2str(domain, 0, domain.length-1));
            return address;
        } catch (Exception e) {
            return null;
        }
    }
    
 
}