<?php
if (isset($_POST['submit'])) {
    function get_data()
    {
        $datae = array();
        $datae[] = array(
            'name' => $_POST['name'],
            'email' => $_POST['email'],
            'message' => $_POST['message'],
            'total' => $_POST['val']
        );
        return json_encode($datae);
    }

    if (file_put_contents(
        "foodFeedback.json",
        get_data()
    )) {
        header("refresh:3;url=/food/");
        echo 'FEEDBACK SUCCESS!';
    } else {
        echo 'There is some error';
    }
}
