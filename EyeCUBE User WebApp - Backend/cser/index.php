<?php
include "../active/active.php";
?>
<!DOCTYPE html>
<!--
	Dimension by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>

<head>
  <title>E | Y | E | C | U | B | E - Service Station</title>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
  <link rel="stylesheet" href="assets/css/main.css" />
  <noscript>
    <link rel="stylesheet" href="assets/css/noscript.css" />
  </noscript>
</head>

<body class="is-preload">
  <!-- Wrapper -->
  <div id="wrapper">
    <!-- Header -->
    <header id="header">
      <div class="logo">
        <span class="icon fa-car"></span>
      </div>
      <div class="content">
        <div class="inner">
          <h1>HELLO <?= $unme ?></h1>
          <p>
            BOOK your car service by
            Just clicking on 2 buttons
          </p>
        </div>
      </div>
      <nav>
        <ul>
          <li><a href="../main/">Return Home</a></li>
          <li><a href="#getdir">Book a service</a></li>
          <li><a href="#fb">Feedback</a></li>
        </ul>
      </nav>
    </header>

    <!-- Main -->
    <div id="main">
      <!-- Intro -->
      <article id="getdir">
        <h2 class="major">Get Location</h2>
        <p>
        <div class="table-wrapper">
          <p id="loc"></p>
          <button type="button" onclick="setLoc()">Book Service Station</button>
          <p id="sloc"></p>
        </div>
        </p>
      </article>

      <!-- Work -->
      <article id="setdir">
        <h2 class="major">Choose Station</h2>
        <p>
        <div class="table-wrapper">
          <form method="post" action="./orderM.php" style="">

          </form>
        </div>
        </p>
      </article>

      <!-- Contact -->
      <article id="fb">
        <h2 class="major">Feedback</h2>
        <form method="post" action="./fback.php">
          <div class="fields">
            <div class="field half">
              <label for="name">Name</label>
              <input type="text" name="name" id="name" required />
            </div>
            <div class="field half">
              <label for="email">Email</label>
              <input type="text" name="email" id="email" required />
            </div>
            <div class="field">
              <label for="message">Message</label>
              <textarea name="message" id="message" rows="4" placeholder="Optional"></textarea>
            </div>
            <div class="field">
              <label for="message">How was your experience?</label>
              <input type="radio" id="good" name="val" />
              <label for="good">Good</label>
              <input type="radio" id="fine" name="val" />
              <label for="fine">Fine</label>
            </div>
          </div>
          <ul class="actions">
            <li>
              <input type="submit" name="submit" value="Send Feedback" class="primary" />
            </li>
            <li><input type="reset" value="Reset" /></li>
          </ul>
        </form>
        <ul class="icons">
          <li>
            <a href="#" class="icon brands fa-twitter"><span class="label">Twitter</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-facebook-f"><span class="label">Facebook</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-instagram"><span class="label">Instagram</span></a>
          </li>
          <li>
            <a href="#" class="icon brands fa-github"><span class="label">GitHub</span></a>
          </li>
        </ul>
      </article>
    </div>

    <!-- Footer -->
    <footer id="footer">
      <p class="copyright">
        &copy; Untitled. Design: <a href="https://html5up.net">HTML5 UP</a>.
      </p>
    </footer>
  </div>

  <!-- BG -->
  <div id="bg"></div>
  <script>
    function getLoc() {
      a = document.getElementById("loc");
      a.innerHTML = "LOCATION: Hosur<br/>";
    }

    function setLoc() {
      getLoc();
      a = document.getElementById("sloc");
      b = document.getElementById("loc");

      if (b == '') {
        a.innerHTML = "Location not found. Click \"Get Location\"";
      } else {
        a.innerHTML = "Booking confirmed <br /> Service station address: <br />1-3-2 10th Cross ABC Road <br /> *For further details check sms";
      }
    }
  </script>

  <!-- Scripts -->
  <script src="assets/js/jquery.min.js"></script>
  <script src="assets/js/browser.min.js"></script>
  <script src="assets/js/breakpoints.min.js"></script>
  <script src="assets/js/util.js"></script>
  <script src="assets/js/main.js"></script>
</body>

</html>