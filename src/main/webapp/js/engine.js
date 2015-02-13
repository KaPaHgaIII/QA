var stompClient = null;
var stompConnected = false;
var sessionId = null;
var onlineSubscription = null;
//CSRF
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

    //load online
    setTimeout(function () {
        $.ajax({
            type: "GET",
            url: "/getOnline",
            success: function (online) {
                showOnline(online.users, online.guests);
            }
        });
    }, 2000);

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

    if (url.match("^/?question")) {
        chat.setUp(url)
    } else if (chat.chatId) {
        chat.finalize();
    }
    if (url == "/" || url.match("^/index")) {
        questions.setUp();
    } else if (questions.isSet) {
        questions.finalize();
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
        this.addEventListener("click", function (event) {
            if (event.which == 2) {
                return true;
            }
            var url = $(this).attr("href"); //get url
            if (url != "/login") {
                history.pushState(null, null, url); //update address bar
            }
            getPage(url); //load page
            event.preventDefault(); //prevent following the link
        }, true);
    });
}
function showOnline(users, guests) {
    var text = "На сайте ";
    if (users) {
        text += users + " " + utils.getUsersWord(users)
    }
    if (users && guests) {
        text += " и ";
    }
    if (guests) {
        text += guests + " " + utils.getGuestsWord(guests);
    }
    $("#online").text(text);
}
function connectWebSocket() {
    var socket = new SockJS('/socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompConnected = true;
        sessionId = socket.sessionId;
        onlineSubscription = stompClient.subscribe("/online", function (message) {
            var online = JSON.parse(message.body);
            showOnline(online.users, online.guests);
        });
    });
}

var questions = {
    loaded: [],
    shown: [],
    earliestTime: new Date().getTime() + 86400000, // 100% max, timezones taken into account
    earliestIds: [], // we need to keep them to avoid reloading already loaded questions
    subscription: undefined,
    isSet: false,
    setUp: function () {
        questions.subscribe();
        questions.loadQuestions(questions.earliestTime);
        $(window).scroll(function () {
            if ($(document).height() - ($(window).scrollTop() + $(window).height()) < 100) {
                questions.loadMore();
            }
        });
        questions.isSet = true;
    },
    subscribe: function () {
        if (stompConnected) {
            this.messagesSubscription = stompClient.subscribe("/questions", function (message) {
                var info = JSON.parse(message.body);
                if (info.type == "subscribers") {
                    questions.setSubscribers(info.questionId, info.value);
                } else if (info.type == "messages") {
                    questions.setMessages(info.questionId, info.value);
                } else if (info.type == "votes") {
                    questions.setVotes(info.questionId, info.value);
                }
            });
        } else { // Please, try again later
            setTimeout(function () {
                questions.subscribe();
            }, 300);
        }
    },
    unsubscribe: function () {
        if (questions.subscription) {
            questions.subscription.unsubscribe();
        }
    },
    loadQuestions: function (time) {
        $.ajax({
            type: "POST",
            url: "/loadQuestions/" + time,
            data: {exclude: questions.earliestIds},
            success: function (response) {
                questions.addQuestions(response);
                questions.showQuestions();
            }
        });
    },
    loadMore: function () {
        questions.loadQuestions(questions.earliestTime);
    },
    addQuestions: function (list) {
        $.each(list, function () {
            var q = this;
            questions.loaded.push(q);
            if (q.updatedTime != questions.earliestTime) {
                questions.earliestIds = [];
            }
            questions.earliestIds.push(q.id);
            questions.earliestTime = Math.min(questions.earliestTime, q.updatedTime);
        });
        questions.sort();
    },
    showQuestions: function () {
        for (var i = 0; i < questions.loaded.length; i++) {
            questions.showQuestion(questions.loaded[i]);
        }
        updateLinks();
    },
    showQuestion: function (q) {
        if (!questions.shown[q.id]) {
            questions.shown[q.id] = true;
            var date = new Date(q.updatedTime);
            var html = [];
            var i = 0;
            html[i++] = "<table class='item' question-id='" + q.id + "'><tr><td class='votes' rowspan='2'>";
            html[i++] = "<div class='number'>" + q.votes + "</div>";
            html[i++] = "<div class='sign'>" + utils.getVotesWord(q.votes) + "</div>";
            html[i++] = "</td><td class='messages' rowspan='2'>";
            html[i++] = "<div class='number'>" + q.messages + "</div>";
            html[i++] = "<div class='sign'>" + utils.getMessagesWord(q.messages) + "</div>";
            html[i++] = "</td><td class='subscribers' rowspan='2'>";
            html[i++] = "<div class='number'>" + q.subscribers + "</div>";
            html[i++] = "<div class='sign'>" + utils.getUsersWord(q.subscribers) + "</div>";
            html[i++] = "</td><td class='title' colspan='2'>";
            html[i++] = "<a href='question?id=" + q.id + "'>" + q.title + "</a>";
            html[i++] = "</td></tr><tr><td class='tags'>";
            $.each(q.tags, function () {
                html[i++] = "<span>" + this.value + "</span> "; // space is important!
            });
            html[i++] = "</td><td class='time'>" + utils.formatDate(date) + "</td>";
            html[i++] = "</tr></table>";

            $("#questions").append(html.join(""));
        }
    },
    setSubscribers: function (questionId, count) {
        var td = $(".item[question-id='" + questionId + "']").find(".subscribers");
        td.find(".number").text(count);
        td.find(".sign").text(utils.getUsersWord(count));
    },
    setMessages: function (questionId, count) {
        var td = $(".item[question-id='" + questionId + "']").find(".messages");
        td.find(".number").text(count);
        td.find(".sign").text(utils.getMessagesWord(count));
    },
    setVotes: function (questionId, count) {
        var td = $(".item[question-id='" + questionId + "']").find(".votes");
        td.find(".number").text(count);
        td.find(".sign").text(utils.getVotesWord(count));
    },
    finalize: function () {
        questions.unsubscribe();
        questions.subscription = undefined;
        questions.isSet = false;
    },
    sort: function () {
        questions.loaded.sort(function (a, b) {
            if (isNaN(a.updatedTime) || isNaN(b.updatedTime)) {
                return a.updatedTime < b.updatedTime ? 1 : -1;
            }
            return b.updatedTime - a.updatedTime;
        });
    }
};
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
    subscribedUsers: [],
    setUp: function (url) {
        var regExp = /^\/?question\?id=(.*)/;
        this.chatId = regExp.exec(url)[1];
        chat.subscribe();
        if (loggedIn) {
            var messageInput = $("#message_input");
            messageInput.prop("disabled", false);
            messageInput.attr("placeholder", "Введите сообщение...");
            var sendButton = $("#send_message_button");
            sendButton.prop("disabled", false);
        }
        //load chat
        $.ajax({
            type: "GET",
            url: "/loadChat/" + this.chatId,
            success: function (response) {
                //load messages
                chat.addMessages(response.messages);
                chat.showMessages();
                //load user votes
                if (loggedIn) {
                    chat.votes[chat.chatId] = [];
                    $.each(response.votedNumbers, function () {
                        var number = this;
                        chat.votes[chat.chatId][number] = true;
                        $(".message[number='" + number + "']").find(".vote_up").addClass("voted");
                    });
                }
                //show users in chat
                var showed = []; // to avoid duplicate
                response.subscribers.unshift({username: myUsername, sessionId: sessionId});
                $.each(response.subscribers, function () {
                    var subscriber = this;
                    // username for anonymous
                    if (subscriber.username == null) {
                        subscriber.username = "";
                    }
                    //continue if duplicate
                    if (showed[subscriber.sessionId]) {
                        return true;
                    }
                    showed[subscriber.sessionId] = true;

                    // init subscribedUsers[] value
                    if (!chat.subscribedUsers[subscriber.username]) {
                        chat.subscribedUsers[subscriber.username] = 0;
                    }
                    // count opened pages for username and anonymous for empty username
                    chat.subscribedUsers[subscriber.username]++;

                    // show if user
                    if (subscriber.username) {
                        chat.showSubscribed(subscriber.username);
                    }
                });
                chat.showAnonymous();
            }
        });
        //scrolling
        var chatContainer = $('#chat_container');
        chatContainer.scroll(function () {
            if (!this.programScroll) {
                this.userScrolledChat = ($("#chat").height() - chatContainer.height()) > chatContainer.scrollTop();
            }
            if (chatContainer.scrollTop() < 50) {
                if (this.firstShownMessage != 0) {
                    this.showMessages(1);
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
        chatContainer.perfectScrollbar({
            includePadding: true
        });
        $("#question_vote_up").click(function () {
            chat.sendQuestionVote(1);
        });
        $("#question_vote_down").click(function () {
            chat.sendQuestionVote(-1);
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
        this.subscribedUsers = [];
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
                } else if (event.action == "questionVote") {
                    chat.receiveQuestionVote(event);
                } else if (event.action == "subscribe") {
                    chat.userSubscribed(event);
                } else if (event.action == "unsubscribe") {
                    chat.userUnsubscribed(event);
                }

            });
        } else { // Please, try again later
            setTimeout(function () {
                chat.subscribe();
            }, 300);
        }
    },
    unsubscribe: function () {
        //reverse order important!
        if (chat.eventsSubscription) {
            chat.eventsSubscription.unsubscribe();
        }
        if (chat.messagesSubscription) {
            chat.messagesSubscription.unsubscribe();
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
            if (chat.votes[chat.chatId] && chat.votes[chat.chatId][m.number]) {
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
            html[i++] = utils.formatDate(date);
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
        var messageDOM = $(".message[number='" + event.number + "']");
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
    },
    sendQuestionVote: function (value) {
        if (loggedIn) {
            if ($("#author").val() != myUsername) {
                var event = {action: "questionVote", value: value};
                stompClient.send("/app/chat/events/" + this.chatId, {}, JSON.stringify(event));
            } else {
                message.showError("Нельзя голосовать за свой вопрос");
            }
        } else {
            message.showError("Вы не авторизированы");
        }
    },
    receiveQuestionVote: function (event) {
        if (event.username == myUsername) {
            var buttonUp = $("#question_vote_up");
            var buttonDown = $("#question_vote_down");
            if (event.result) {
                if (event.value > 0) {
                    buttonUp.addClass("voted");
                    buttonDown.removeClass("voted");
                } else {
                    buttonDown.addClass("voted");
                    buttonUp.removeClass("voted");
                }
            } else {
                buttonUp.removeClass("voted");
                buttonDown.removeClass("voted");
            }
        }
        $("#question_votes").text(event.number);
    },
    userSubscribed: function (event) {
        if (!event.subscriber.username) {
            event.subscriber.username = "";
        }
        // init subscribedUsers[] value
        if (!chat.subscribedUsers[event.subscriber.username]) {
            chat.subscribedUsers[event.subscriber.username] = 0;
        }
        // count opened pages for username and anonymous for empty username
        chat.subscribedUsers[event.subscriber.username]++;
        if (event.subscriber.username) {
            chat.showSubscribed(event.subscriber.username);
        }
        chat.showAnonymous();
    },
    userUnsubscribed: function (event) {
        if (!event.subscriber.username) {
            event.subscriber.username = "";
        }
        // count opened pages for username and anonymous for empty username
        chat.subscribedUsers[event.subscriber.username]--;

        if (event.subscriber.username) {
            chat.removeSubscribed(event.subscriber.username);
        }
        chat.showAnonymous();
    },
    showSubscribed: function (username) {
        if (chat.subscribedUsers[username] == 1) {
            $("#users_in_chat .anon_users").before("<li username='" + username + "'>" + username + "</li>")
        }
    },
    removeSubscribed: function (username) {
        if (chat.subscribedUsers[username] == 0) {
            $("#users_in_chat li[username='" + username + "']").remove();
        }
    },
    showAnonymous: function () {
        var anonCount = chat.subscribedUsers[""];
        var anonDOM = $("#users_in_chat .anon_users");
        if (anonCount) {
            // do we need " и"?
            var and = "";
            for (var username in chat.subscribedUsers) {
                // first becomes false when "" (anon), last - when chat.subscribedUsers[username] == 0
                if (username && chat.subscribedUsers.hasOwnProperty(username) && chat.subscribedUsers[username]) {
                    and = "и ";
                    break;
                }
            }
            anonDOM.html(and + anonCount + utils.getGuestsWord(anonCount));
        } else {
            anonDOM.html("");
        }
    }
};
var utils = {
    formatDate: function (date) {
        var today = new Date();
        if (date.getDate() == today.getDate() &&
            date.getMonth() == today.getMonth() &&
            date.getYear() == today.getYear()) {
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
        } else {
            var dd = date.getDate();
            if (dd < 10) {
                dd = "0" + dd;
            }
            var mm = date.getMonth() + 1;
            if (mm < 10) {
                mm = "0" + mm;
            }
            return dd + "." + mm;
        }
    },
    getGuestsWord: function (count) {
        count = Math.abs(count);
        if (count % 100 >= 10 && count % 100 <= 20) {
            return " гостей";
        }
        switch (count % 10) {
            case 1:
                return " гость";
            case 2:
            case 3:
            case 4:
                return " гостя";
            default:
                return " гостей";
        }
    },
    getUsersWord: function (count) {
        count = Math.abs(count);
        if (count % 100 >= 10 && count % 100 <= 20) {
            return " пользователей";
        }
        switch (count % 10) {
            case 1:
                return " пользователь";
            case 2:
            case 3:
            case 4:
                return " пользователя";
            default:
                return " пользователей";
        }
    },
    getVotesWord: function (count) {
        count = Math.abs(count);
        if (count % 100 >= 10 && count % 100 <= 20) {
            return " голосов";
        }
        switch (count % 10) {
            case 1:
                return " голос";
            case 2:
            case 3:
            case 4:
                return " голоса";
            default:
                return " голосов";
        }
    },
    getMessagesWord: function (count) {
        count = Math.abs(count);
        if (count % 100 >= 10 && count % 100 <= 20) {
            return " сообщений";
        }
        switch (count % 10) {
            case 1:
                return " сообщение";
            case 2:
            case 3:
            case 4:
                return " сообщения";
            default:
                return " сообщений";
        }
    },
    sort: function (arr) {
        arr.sort(function (a, b) {
            if (isNaN(a) || isNaN(b)) {
                return a > b ? 1 : -1;
            }
            return a - b;
        });
        return arr;
    }
};

var message = {
    headerDOM: undefined,
    hideTimeout: undefined,
    opa: 100,
    x: 1,
    showError: function (text) {
        var messageDOM = $("#message");
        messageDOM.text(text);
        messageDOM.removeClass("success");
        messageDOM.addClass("error");
        this.prepareHide();
    },
    showSuccess: function (text) {
        var messageDOM = $("#message");
        messageDOM.text(text);
        messageDOM.removeClass("error");
        messageDOM.addClass("success");
        this.prepareHide();
    },
    prepareHide: function () {
        this.headerDOM = $("#header_wr");
        this.headerDOM.css("opacity", "0");
        this.opa = 0;
        this.x = 1;
        clearTimeout(message.hideTimeout);
        this.hideTimeout = setTimeout(message.hide, 1000);
    },
    hide: function () {
        message.opa = message.opa + message.x;
        message.x = message.x * 1.3;
        message.headerDOM.css("opacity", message.opa / 100);
        if (message.opa < 100) {
            message.hideTimeout = setTimeout(message.hide, 50)
        }
    }
};