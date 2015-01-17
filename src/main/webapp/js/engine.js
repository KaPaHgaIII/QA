var stompClient = null;
var stompConnected = false;

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
        chat.setUp(url)
    } else if (chat.chatId) {
        chat.finalize();
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

function connectWebSocket() {
    var socket = new SockJS('/socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompConnected = true;
    });
}


var chat = {
    votes: [],
    chatId: undefined,
    messagesSubscription: undefined,
    eventsSubscription: undefined,
    messages: [],
    lastMessage: -1,
    firstShownMessage: Infinity,
    lastShownMessage: -1,
    userScrolledChat: false, // defines if we should scroll when new message coming
    programScroll: false, // true - program scrolling, false - user scrolling (for scroll event)
    setUp: function (url) {
        var regExp = /^\/question\?id=(.*)/;
        this.chatId = regExp.exec(url)[1];

        this.subscribe();

        if (loggedIn) {
            var messageInput = $("#message_input");
            messageInput.prop("disabled", false);
            messageInput.attr("placeholder", "Введите сообщение...");
            messageInput.focus();

            var sendButton = $("#send_message_button");
            sendButton.prop("disabled", false);
        }

        //load current messages
        $.ajax({
            type: "GET",
            url: "/loadChatMessages/" + this.chatId,
            success: function (msgs) {
                chat.addMessages(msgs);
                chat.showMessages();
            }
        });
        
        
        //load votes
        if(loggedIn && !chat.votes[chat.chatId]) {
            $.ajax({
                type: "GET",
                url: "/loadChatVotes/" + this.chatId,
                success: function (numbers) {
                    chat.votes[chat.chatId] = [];
                    $.each(numbers, function () {
                        var number = this;
                        chat.votes[chat.chatId][number] = true;
                        $(".message[number='" + number + "']").find(".vote_up").addClass("voted");
                    });
                }
            });
        }


        //scrolling
        var chatContainer = $('#chat_container');
        chatContainer.scroll(function () {

            if (!this.programScroll) {
                this.userScrolledChat = ($("#chat").height() - chatContainer.height()) > chatContainer.scrollTop();
            }

            if (chatContainer.scrollTop() < 50) {

                if (this.firstShownMessage != 0) {
                    this.showMessages(1);
                    //alert("I should show you previous messages")
                }
            }
        });

        $("#message_input").keypress(function (e) {
            if (e.keyCode == 13) {
                chat.sendMessage();
            }
        });
        $("#send_message_button").click(function () {
            chat.sendMessage();
        });

        //voting
        $("#chat").click(function (e) {
            var target = $(e.target);
            if (target.hasClass("vote_up")) {
                chat.sendVote(target.parents(".message").attr("number"));
            }
        });
    },
    finalize: function () {
        this.unsubscribe();
        this.chatId = undefined;
        this.messagesSubscription = undefined;
        this.messages = [];
        this.lastMessage = -1;
        this.firstShownMessage = Infinity;
        this.lastShownMessage = -1;
        this.userScrolledChat = false;
        this.programScroll = false;
    },
    subscribe: function () {
        if (stompConnected) {
            this.messagesSubscription = stompClient.subscribe("/chat/messages/" + this.chatId, function (message) {
                chat.addMessages(JSON.parse(message.body));
                chat.showMessages();
            });
            this.eventsSubscription = stompClient.subscribe("/chat/events/" + this.chatId, function (message) {
                var event = JSON.parse(message.body);
                if (event.action == "vote") {
                    chat.receiveVote(event);
                }
            });
        } else { // Please, try again later
            setTimeout(function () {
                chat.subscribe();
            }, 300);
        }
    },
    unsubscribe: function () {
        if (this.messagesSubscription) {
            this.messagesSubscription.unsubscribe();
        }
        if (this.eventsSubscription) {
            this.eventsSubscription.unsubscribe();
        }
    },
    addMessages: function (msgs) {
        for (var i = 0; i < msgs.length; i++) {
            this.addMessage(msgs[i]);
        }
    },
    addMessage: function (message) {
        this.lastMessage = Math.max(this.lastMessage, parseInt(message.number));
        if (this.messages[message.number] == undefined) { // do not overwrite existing
            this.messages[message.number] = message;
        }
    },
    showMessages: function (action) {
        var chatDOM = $("#chat");
        var chatContainer = $("#chat_container");
        if (action) {
            if (action == 1) { // show previous 25
                this.programScroll = true;
                var scrolledBottom = chatDOM.height() - chatContainer.scrollTop();
                var i = this.firstShownMessage - 1;
                var shown = 0;
                while (i > -1 && shown < 26) {
                    if (this.messages[i] != undefined) {
                        this.showMessage(this.messages[i]);
                        shown++;
                    }
                    i--;
                }
                chatContainer.scrollTop(chatDOM.height() - scrolledBottom);
                this.programScroll = false;
            }
        }
        if (this.lastShownMessage != -1) { // for new incoming messages
            for (var j = this.lastShownMessage; j <= this.lastMessage; j++) {
                if (this.messages[j] != undefined) {
                    this.showMessage(this.messages[j]);
                }
            }
            if (!this.userScrolledChat) {
                this.programScroll = true;
                chatContainer.scrollTop(chatDOM.height() + 99999);
                this.programScroll = false;
            }
        }
        if (this.firstShownMessage == Infinity) { // initial (last 25 messages)
            var i = this.lastMessage;
            var shown = 0;
            while (i > -1 && shown < 26) {
                if (this.messages[i] != undefined) {
                    this.showMessage(this.messages[i]);
                    shown++;
                }
                i--;
            }
            chatContainer.scrollTop(chatDOM.height() + 99999);
        }
    },
    showMessage: function (m) {
        if (!m.shown) {

            m.shown = true;
            var date = new Date(m.time);

            var html = [];
            var i = 0;
            html[i++] = "<table class='message' number='";
            html[i++] = m.number;
            html[i++] = "'><tr><td class='votes'><span class='vote_up";
            if (!myUsername || myUsername == m.username) {
                html[i++] = " disabled";
            }
            if(chat.votes[chat.chatId] && chat.votes[chat.chatId][m.number]){
                html[i++] = " voted";
            }
            html[i++] = "'></span><span class='votes_count'>";
            if (m.votes > 0) {
                html[i++] = m.votes;
            }
            html[i++] = "</span></td><td class='name_text'><span class='username'>";
            html[i++] = m.username;
            html[i++] = ": </span><span class='text'>";
            html[i++] = m.text;
            html[i++] = "</span></td><td class='time'>";
            html[i++] = formatDate(date);
            html[i++] = "</td></tr></table>";

            var message = html.join("");

            if (this.lastShownMessage == -1) {
                $("#chat").append(message);
            } else {
                var diff = 1;
                var n;
                while (true) {
                    n = m.number + diff;
                    if (this.messages[n] && this.messages[n].shown) {
                        $(".message[number='" + n + "']").before(message);
                        break;
                    }
                    n = m.number - diff;
                    if (this.messages[n] && this.messages[n].shown) {
                        $(".message[number='" + n + "']").after(message);
                        break;
                    }
                    diff++;
                }
            }

            this.firstShownMessage = Math.min(m.number, this.firstShownMessage);
            this.lastShownMessage = Math.max(m.number, this.lastShownMessage);

        }
    },
    sendMessage: function () {
        if (loggedIn) {
            var messageInput = $("#message_input");
            var text = messageInput.val();
            if (text) {
                stompClient.send("/app/chat/messages/" + this.chatId, {}, JSON.stringify({text: text}));
            }
            messageInput.val("");
            messageInput.focus();
        }
    },
    sendVote: function (number) {
        if (loggedIn) {
            var event = {action: "vote", number: number};
            stompClient.send("/app/chat/events/" + this.chatId, {}, JSON.stringify(event));
        }
    },
    receiveVote: function (event) {
        //alert(event.result);
        var messageDOM = $(".message[number='" + event.number + "']");
        //messageDOM.css("background", "red");
        if (event.username == myUsername) {
            if (event.result) {
                messageDOM.find(".vote_up").addClass("voted");
                chat.votes[chat.chatId][event.number] = true;
            } else {
                messageDOM.find(".vote_up").removeClass("voted");
                chat.votes[chat.chatId][event.number] = undefined;
            }
        }
        var votesCountDOM = messageDOM.find(".votes_count");
        var sign = event.result ? 1 : -1;
        var votes = parseInt(0 + votesCountDOM.html()) + sign;
        votesCountDOM.html(votes == 0 ? "" : votes);


    }
};


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