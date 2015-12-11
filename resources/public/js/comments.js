function postComment() {

   var commentToPost = $("#comment_box").val();
   var photoId = $(".photoview-image").attr("id");

   if (commentToPost.length < 1) {
        console.log("blank comment!");
        alert("Please don't post blank comments.");
        return;
   }

   //POST Ajax call
   $.post("/add-comment",
   { new_comment: commentToPost, photo_id: photoId },
   function (response) {

        if(response.status === "ok") {

                console.log("response successful");
               // posting successful
               var userName = response.poster_name;

               // insert new div in comments
               var comment = "<div class='comment'>" +
               "<h5>" + userName + "</h5>" +
               "<p>" + commentToPost + "</p>" +
               "<p>" + response.time + "</p></div>";

               $(comment).insertAfter("#photo-comments-header");

               $("#photoview-success-wrapper").css("display", "block");
               $("#photoview-success").append(
                $('<li>', {html: "Comment posted."}));


        } else {

            console.log("response failure: " + response.status);
            // there was an error posting the comment

             $("#photoview-error-wrapper").css("display", "block");
             $("#photoview-error").append(
               $('<li>', {html: "Error: " + response.status}));

        }

   },
   "json");

}

// executed on page load
$(document).ready(function(){
    $("#comment_button").click(postComment); // add onclick listener to delete button
});