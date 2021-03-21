<?php
if (isset($_POST['submit'])) {
    function get_data()
    {
        $datae = array();
        $datae[] = array(
            'suit' => $_POST['aone'],
            'tee' => $_POST['athree'],
            'trackp' => $_POST['atwo'],
            'sand' => $_POST['afour'],
        );
        return json_encode($datae);
    }

    if (file_put_contents(
        "apparelOrder.json",
        get_data()
    )) {
        header("refresh:3;url=/main/");
        echo 'ORDER SUCCESS!';
    } else {
        echo 'There is some error';
    }
}
