package com.trivialgaming.HttpServer;

public class HttpParser {
    final static String NL = "\n\r";

    HttpParser(){

    }

    public static String CreateResponse(String content, String content_type){
        HttpResponse ret= new HttpResponse(content);
        ret.Content_Type = content_type;
        ret.Code = "200";
        ret.Status = ret.GetStatus(ret.Code);

        return ret.GetString();
    }

    public static String CreateResponse_403(String content, String content_type){
        HttpResponse ret= new HttpResponse(content);
        ret.Content_Type = content_type;
        ret.Code = "403";
        ret.Status = "OK";

        return ret.GetString();
    }



    public static class HttpRequest{
        public String method;
        public String file;
        public String raw;

        HttpRequest(String request){
            this.raw = request;
            method = GetMethod();
            file = GetFile();
        }
        private String GetMethod(){
            String ret = raw.split(" ")[0];
            return ret;
        }
        private String GetFile() {
            String line = raw.split("\n")[0]; // First line
            int begin = line.indexOf("/");
            //No file
            if(begin == -1) return "";
            int end = line.indexOf(" ",begin+1);
            if(end == -1) return "";
            String ret = line.substring(begin+1,end);
            //Check if "/"
            if(ret.length() == 0){
                return "/";
            }
            return ret;
        }


    }

    public static class HttpResponse{
        public String HTTP_Version = "HTTP:1/1";
        public String Code = "200";
        public String Status;
        public String Content_Type = "";
        private String Content = "";

        HttpResponse(String content){
            this.Content = content;
        }

        public static String GetStatus(String code){
            switch(code){
                case "200":
                    return "OK";
                case "403":
                    return "Forbidden";
                case "405":
                    return "Unsupported";
            }

            return "";

        }

        public String GetString(){
            String ret=
                    "HTTP/1.1" + " " + Code + " " + GetStatus(Code) + NL +
                            "Content-Length: " + Content.getBytes().length + NL +
                            "Content-Type: " + Content_Type + NL +
                            NL +
                            Content + NL;

            return ret;
        }
    };

}
