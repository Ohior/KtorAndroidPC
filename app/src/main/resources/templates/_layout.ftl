<#macro header>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Ohis Journal</title>
    <link rel="icon" href="/static/image.png">
    <style>
        #custom1 {
          background-color: lightgrey;
          color: red;
          padding: 40px;
          text-align: center;
        }
        #custom2 {
          background-color: lightgrey;
          color: yellow;
          padding: 40px;
          text-align: center;
        }
        #custom3 {
          background-color: lightgrey;
          color: blue;
          padding: 40px;
          text-align: center;
        }
    </style>
</head>
<body style="text-align: center; font-family: sans-serif">
<table>
    <tr>
        <th id="custome1"><button type="submit">Back</button></th>
        <th id="custome1"><button type="submit">Back</button></th>
    </tr>
</table>
<#nested>
</body>
</html>
</#macro>