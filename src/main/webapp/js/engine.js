var stompClient = null;
var stompConnected = false;
var subscribedChatId = null; //current chat
var chatSubscription = null;

var chatMessages = [];
var lastMessage = -1;
var firstShownMessage = Infinity;
var lastShownMessage = -1;
var userScrolledChat = false; // defines if we should scroll when new message coming
var programScroll = false; // true - program scrolling, false - user scrolling

$(function () {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
});

//first time come or page refresh
$(document).ready(function () {

    connectWebSocket();

    $("#logout_button").click(function () {
        $("#logout_form").submit();
    });

    //load content
    var url = location.pathname + location.search + location.hash;
    getPage(url);

    //add history listener
    window.setTimeout(function () { //fix for chrome
        window.addEventListener("popstate", function () {
            //when pressed back or forward in browser
            //loading content, corresponding to the new url
            var url = location.pathname + location.search + location.hash;
            getPage(url);
        }, false);
    }, 1);


});


function getPage(url) {
    var hash = window.location.hash.substring(1);
    if (hash == "loginError") {
        url = "/login?error"
    }
    $.ajax({
        type: "GET",
        url: url,
        success: function (data) {
            loadPage(url, data);
        }
    });
}

function loadPage(url, data) {

    $("#content").remove(); //remove old content, bound events and jquery data
    $("#content_container").html(data);
    setTitle();
    updateLinks();

    //websocket maintenance
    if (url.match("^/question")) {
        setUpChat(url)
    } else if (subscribedChatId) {
        unsubscribeChat(subscribedChatId);
    }
}

function setTitle() {
    var titleHolder = $("#title");
    document.title = titleHolder.val();
    titleHolder.remove();
}

//add click event listener to <a> tags
function updateLinks() {
    $.each($("a"), function () {
        this.addEventListener("click", function (e) {
            var url = $(this).attr("href"); //get url
            if (url != "/login") {
                history.pushState(null, null, url); //update address bar
            }
            getPage(url); //load page
            e.preventDefault(); //prevent following the link
        }, true);
    });
}

function setUpChat(url) {
    var regExp = /^\/question\?id=(.*)/;
    var chatId = regExp.exec(url)[1];

    subscribeChat(chatId);

    if (loggedIn) {
        var messageInput = $("#message_input");
        messageInput.prop("disabled", false);
        messageInput.attr("placeholder", "Введите сообщение...");
        messageInput.focus();

        var sendButton = $("#send_message_button");
        sendButton.prop("disabled", false);
    }

    $.ajax({
        type: "GET",
        url: "/loadChatMessages/" + chatId,
        success: function (msgs) {
            addMessages(msgs);
            showMessages();
        }
    });

    var chatContainer = $('#chat_container');

    chatContainer.scroll(function () {

        if (!programScroll) {
            userScrolledChat = ($("#chat").height() - chatContainer.height()) > chatContainer.scrollTop();
            //alert(userScrolledChat);
        }

        if (chatContainer.scrollTop() < 50) {

            if (firstShownMessage != 0) {
                showMessages(1);
                //alert("I should show you previous messages")
            }
        }
    });
}

function addMessages(msgs) {
    for (var i = 0; i < msgs.length; i++) {
        addMessage(msgs[i]);
    }
}

function addMessage(message) {
    lastMessage = Math.max(lastMessage, parseInt(message.number));
    if (chatMessages[message.number] == undefined) {
        chatMessages[message.number] = message;
    }
}

function showMessages(action) {
    var chat = $("#chat");
    var chatContainer = $("#chat_container");
    if (action) {
        if (action == 1) { // show previous 25
            programScroll = true;
            var scrolledBottom = chat.height() - chatContainer.scrollTop();
            var i = firstShownMessage - 1;
            var shown = 0;
            while (i > -1 && shown < 26) {
                if (chatMessages[i] != undefined) {
                    showMessage(chatMessages[i]);
                    shown++;
                }
                i--;
            }
            chatContainer.scrollTop(chat.height() - scrolledBottom);
            programScroll = false;
        }
    }
    if (lastShownMessage != -1) { // for new incoming messages
        for (var j = lastShownMessage; j <= lastMessage; j++) {
            if (chatMessages[j] != undefined) {
                showMessage(chatMessages[j]);
            }
        }
        if(!userScrolledChat){
            programScroll = true;
            chatContainer.scrollTop(chat.height() + 99999);
            programScroll = false;
        }
    }
    if (firstShownMessage == Infinity) { // initial (last 25 messages)
        var i = lastMessage;
        var shown = 0;
        while (i > -1 && shown < 26) {
            if (chatMessages[i] != undefined) {
                showMessage(chatMessages[i]);
                shown++;
            }
            i--;
        }
        chatContainer.scrollTop(chat.height() + 99999);
    }
}

function showMessage(m) {
    if (!m.shown) {

        m.shown = true;
        var date = new Date(m.time);

        var html = [];
        var i = 0;
        html[i++] = "<table class='message' number='";
        html[i++] = m.number;
        html[i++] = "'><tr><td class='name_text'><span class='username'>";
        html[i++] = m.username;
        html[i++] = ": </span><span class='text'>";
        html[i++] = m.text;
        html[i++] = "</span></td><td class='time'>";
        html[i++] = formatDate(date);
        html[i++] = "</td></tr></table>";

        var message = html.join("");

        if (lastShownMessage == -1) {
            $("#chat").append(message);
        } else {
            var diff = 1;
            var n;
            while (true) {
                n = m.number + diff;
                if (chatMessages[n] && chatMessages[n].shown) {
                    $(".message[number='" + n + "']").before(message);
                    break;
                }
                n = m.number - diff;
                if (chatMessages[n] && chatMessages[n].shown) {
                    $(".message[number='" + n + "']").after(message);
                    break;
                }
                diff++;
            }
        }

        firstShownMessage = Math.min(m.number, firstShownMessage);
        lastShownMessage = Math.max(m.number, lastShownMessage);

    }
}

function connectWebSocket() {
    var socket = new SockJS('/socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompConnected = true;
        console.log('Connected: ' + frame);
    });
}

function subscribeChat(chatId) {
    if (stompConnected) {
        chatSubscription = stompClient.subscribe("/chat/" + chatId, function (message) {
            addMessages(JSON.parse(message.body));
            showMessages();
        });
        subscribedChatId = chatId;

        console.log('subscribed to chat: ' + chatId);
    } else { // Please, try again later
        setTimeout(function () {
            subscribeChat(chatId);
        }, 300);
    }
}

function unsubscribeChat(chatId) {
    if (chatSubscription) {
        chatSubscription.unsubscribe();
        console.log('unsubscribed from chat: ' + chatId);
    }
}

function sendMessage() {
    if (loggedIn) {
        var messageInput = $("#message_input");
        var text = messageInput.val();
        if (text) {
            stompClient.send("/app/chat/" + subscribedChatId, {}, JSON.stringify({text: text}));
        }
        messageInput.val("");
        messageInput.focus();
    }
}

function formatDate(date) {

    var hh = date.getHours();
    if (hh < 10) {
        hh = "0" + hh;
    }

    var mm = date.getMinutes();
    if (mm < 10) {
        mm = "0" + mm;
    }

    var ss = date.getSeconds();
    if (ss < 10) {
        ss = "0" + ss;
    }

    return hh + ":" + mm + ":" + ss;
}