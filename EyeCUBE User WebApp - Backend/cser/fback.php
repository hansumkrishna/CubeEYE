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
        "cserviceFeedback.json",
        get_data()
    )) {
        header("refresh:3;url=/main/");
        echo 'FEEDBACK SUCCESS!';
    } else {
        echo 'There is some error';
    }
}
