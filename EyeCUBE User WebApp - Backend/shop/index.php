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
  <title>E | Y | E | C | U | B | E - Shop</title>
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
        <span class="icon fa-cookie"></span>
      </div>
      <div class="content">
        <div class="inner">
          <h1>HELLO <?= $unme ?></h1>
          <p>
            What do you wanna eat?
          </p>
        </div>
      </div>
      <nav>
        <ul>
          <li><a href="../home/">Return Home</a></li>
          <li><a href="#ap">Apparel</a></li>
          <li><a href="#sg">Gadgets</a></li>
          <li><a href="#fb">Feedback</a></li>
        </ul>
      </nav>
    </header>

    <!-- Main -->
    <div id="main">
      <!-- Intro -->
      <article id="ap">
        <h2 class="major">Clothes & Shoes</h2>
        <p>
        <div class="table-wrapper">
          <table class="alt">
            <form method="post" action="./orderA.php" style="">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Price</th>
                  <th colspan="2">Size (in cms)</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><input type="hidden" />Suit (Entire set)</td>
                  <td>250</td>
                  <td><input type="text" id="one" name="aone" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" />Track Pant - Blue</td>
                  <td>350</td>
                  <td><input type="text" id="two" name="atwo" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" />Shirt - White</td>
                  <td>380</td>
                  <td><input type="text" id="three" name="athree" placeholder="0" /></td>
                </tr>
                <tr>
                  <td><input type="hidden" />Sandals</td>
                  <td>300</td>
                  <td><input type="text" id="four" name="afour" placeholder="0" /></td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td><button type="button" onclick="calc()">Calculate Total</button></td>
                  <td><button type="submit" name="submit" onclick="change(this.id)">Pay</button></td>
                  <td><input id="total" name="tot" style="color:black;" readonly value="Rs. 0" /></td>
            </form>
            </tr>
            </tfoot>
          </table>
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
    var aone = document.getElementById("one");
    var atwo = document.getElementById("two");
    var athree = document.getElementById("three");
    var afour = document.getElementById("four");

    function isE(a, e) {
      if (!a.value) {
        return 0;
      } else {
        return e;
      }
    }

    function change(a) {
      alert('PAID!');
    }

    function calc() {
      var u = isE(aone, 250);
      var v = isE(atwo, 350);
      var w = isE(athree, 380);
      var x = isE(afour, 300);
      var tbill = eval(u +
        '+' + v + '+' + w +
        '+' + x);
      total.value = 'Rs. ' + tbill;
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