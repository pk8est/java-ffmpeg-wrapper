<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">

<head>

    <title>Video.js | HTML5 Video Player</title>
    <link href="/resource/css/video-js.css" rel="stylesheet">
    <link href="/resource/css/videojs-resolution-switcher.css" rel="stylesheet">
    <script src="/resource/js/jquery.min.js"></script>
    <script src="/resource/js/videojs-ie8.min.js"></script>
    <script src="/resource/js/video.js"></script>
    <script src="/resource/js/videojs-contrib-hls.min.js"></script>
    <%--<script src="/resource/js/videojs-contrib-hlsjs.js"></script>--%>
    <script src="/resource/js/videojs5.hlsjs.js"></script>
    <script src="/resource/js/videojs-resolution-switcher.js"></script>
    <script src="http://vhuya.dwstatic.com/video/dot/demo/swfobject.js"></script>
    <style type="text/css">
        body{margin: 0 auto; width: 1080px;}
        ul{list-style: none;margin-left: 0;padding-left: 0}
        .input{width: 600px;}
        .input-div{margin-bottom: 5px;}
        #video{margin-top: 20px;}
    </style>
</head>
<body>
    <h3>HLS播放器</h3>
    <div class="input-div">
        URL: <input type="text" id="url" class="input">
        <button onclick="updatePlay(false)">播放</button>
        <button onclick="updatePlay(true)">flash播放</button>
        <button onclick="updatePlay(true, true)">flash2播放</button>
    </div>
    <div  class="input-div">
        UID: <input type="text" id="uid" class="input">
        <button onclick="livePlay()">直播</button>
        <button onclick="playbackPlay()">回放</button>
        <button onclick="playbackPlay(true)">回放列表</button>
    </div>
    <div id="video">

    </div>
    <div id="history">
        <ul></ul>
    </div>

<script>

    function updatePlay(flash, flash){
        var url = $("#url").val()
        if(!url || url.indexOf("http") != 0){
            return alert("请输入url地址!");
        }
        var m3u8 = url.indexOf(".m3u8")!=-1 ? true : false;
        if(flash){
            flashPlayer(url)
        }else{
            play(url, m3u8, flash)
        }
        $("#history ul").append("<li>URL: "+url+"</li>")
    }

    function livePlay(){
        var uid = $("#uid").val()
        var url = "http://dot.v.duowan.com/index.php?r=huya/getliveurl&type=m3u8&yyuid=" + uid
        $.get(url, function (data) {
            var url = data.hasOwnProperty("url") ? data.url : "";
            if(url){
                play(url, url.indexOf(".m3u8") != -1)
            }else{
                alert("没有开播")
            }
        }, "jsonp")
        $("#history ul").append("<li>UID: "+uid+"</li>")
    }

    function playbackPlay(list){
        var uid = $("#uid").val()
        var url = "http://v.huya.com/index.php?r=zhubo/playback&uid=" + uid
        $.get(url, function (data) {
            var url = data.hasOwnProperty("data") ? data.data.video_url : "";
            if(url){
                if(list) url = url.replace("vod.m3u8", "vod-playlist.m3u8");
                play(url, url.indexOf(".m3u8") != -1)
            }else{
                alert("没有回放")
            }
        }, "jsonp")
        $("#history ul").append("<li>UID: "+uid+"</li>")

    }

    function play(url, m3u8, flash){
        console.info
        if(m3u8){
            var html = '<video id="video_item" class="video-js vjs-big-play-centered vjs-skin-colors-blue"><source type="application/x-mpegURL" controls src="'+url+'"></source>您的浏览器不支持 video 标签</video>';
        }else{
            var html = '<video id="video_item" class="video-js vjs-big-play-centered vjs-skin-colors-blue"><source type="video/mp4" controls src="'+url+'"></source>您的浏览器不支持 video 标签</video>';
        }
        $("#video").html(html)
        if(window.player){
            window.player.dispose();
        }
        var options = {hls: {withCredentials: true}};
        var player = videojs('video_item', {
            controls: true,
            preload: "meta",
            width: 960,
            height: 544,
            techOrder: flash ? ["flash"] : ["html5", "flash"],
            preload: "auto",
            autoplay: true,
            //flash: options,
            //html5: options,
            controlBar: {
                remainingTimeDisplay: false
            },
            plugins: {
                videoJsResolutionSwitcher: {
                    default: "360"
                }
            }
        }, function(){
            player.on('resolutionchange', function(){
                console.log('Source changed to %s', player.src());
                var setRes = player.currentResolution();
            })
            window.player = player;
        });
    }

    function flashPlayer(url){
        var flashvars = {
            url: url,
            auto_play: 1,
            start: 0,
            end: 0,
            sc: "sc",
            ec: "ec"
        };
        var params = {};
        params.quality = "high";
        params.bgcolor = "#000000";
        params.allowscriptaccess = "always";
        params.allowfullscreen = "true";
        var attributes = {};
        attributes.id = "v-dot-editor-player";
        attributes.name = "v-dot-editor-player";
        attributes.align = "middle";
        swfobject.embedSWF("http://vhuya.dwstatic.com/video/dot/v-dot-editor-player.swf", "video", "800", "600", "10.2.0", "", flashvars, params, attributes)

        window["sc"] = function (start) {  };
        window["ec"] = function (end) {  };
    }
</script>

</body>

</html>
