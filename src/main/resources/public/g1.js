$(() => {
    wxd.netty.post("/web-title", "",
        function (responseText) {
            if (responseText.code === 1) {
                window.title = responseText.data + " - " + window.title;
            }
        },
        function (error) {
            console.log("11->" + error);
        }
    );
});