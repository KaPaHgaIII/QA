VK.Widgets.Auth("vk_auth", {
    width: "200px",
    onAuth: function (data) {
        register.vkData = data;
        $.ajax({
            type: "POST",
            url: "/vk_login",
            data: {vkUid: data['uid'], hash: data['hash']},
            success: function (result) {
                if (result == "need_username") {
                    getPage("need_username");
                } else if (result == "failed") {
                    window.location = "/";
                } else {
                    window.location.reload();
                }
            }
        });
    }
});