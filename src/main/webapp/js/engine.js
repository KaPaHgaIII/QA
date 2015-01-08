var stompClient = null;
var stompConnected = false;
var subscribedChatId = null; //current chat
var chatSubscription = null;

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
        success: function (message) {
            showMessages(message);
        }
    });
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
            showMessages(JSON.parse(message.body));
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

function showMessages(msgs) {
    for (var i = 0; i < msgs.length; i++) {
        showMessage(msgs[i]);
    }
}

function showMessage(m) {
    var date = new Date(m.time);

    var username = $("<span class='username'>" + m.username + ": </span>");
    var text = $("<span class='text'>" + m.text + "</span>");
    var nameText = $("<td class='name_text'></td>");
    nameText.append(username);
    nameText.append(text);
    var time = $("<td class='time'>" + formatDate(date) + "</td>");
    var tr = $("<tr></tr>");
    tr.append(nameText);
    tr.append(time);
    var message = $("<table class='message'></table>");
    message.append(tr);

    var chat = $("#chat");
    chat.append(message);

    $('#chat_container').scrollTop(chat.height());
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
