VK.Widgets.Auth("vk_auth", {
    width: "200px",
    onAuth: function (data) {
        $.ajax({
            type: "POST",
            url: "/vk_attach",
            data: {vkUid: data['uid'], hash: data['hash']},
            success: function (result) {
                var vkRestr = $(".restrictions.vk");
                if (result == "failed") {
                    vkRestr.css("color", "red");
                    vkRestr.text("При привязке аккаунта ВКонтакте проищошла ошибка")
                } else if (result == "vkUid_busy") {
                    vkRestr.css("color", "red");
                    vkRestr.text("Этот аккаунт ВКонтакте уже привязан к другому пользователю")
                } else if (result == "success") {
                    goToPage("/cp", true);
                }
            }
        });
    }
});