<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    function get_data()
    {
        $datae = array();
        $datae[] = array(
            'name' => $_POST['name'],
            'phone' => $_POST['phno'],
            'email' => $_POST['email'],
            'pass' => $_POST['pass'],

        );
        return json_encode($datae);
    }

    function correctFile()
    {
        $jsonString = file_get_contents('loginData.json');
        for ($i = 0; $i < strlen($jsonString); $i++) {
            if ($jsonString[$i] == '[' && $i != 0) {
                $jsonString[$i] = ' ';
            } else if ($jsonString[$i] == ']' && $i != strlen($jsonString) - 2) {
                $jsonString[$i] = ',';
            }
        }
        file_put_contents('loginData.json', $jsonString);
    }

    $name = "loginData";
    $file_name = $name . '.json';

    if (file_put_contents(
        "$file_name",
        get_data() . PHP_EOL,
        FILE_APPEND
    )) {
        header("refresh:3;url=/login/");
        echo 'REGISTER OK';
    } else {
        echo 'There is some error';
    }

    correctFile();
}
