<?php

use Slim\Http\Request;
use Slim\Http\Response;

$app->get('/[{name}]', function ($request, $response, $args) {
    // Sample log message
    $this->logger->info("Slim-Skeleton '/' route");

    // Render index view
    return $this->renderer->render($response, 'index.phtml', $args);
});


$app->get('/input/', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();

    $input = $params['i'] ?? null;

    if (is_null($input) || empty($input)) {
        return $response->withRedirect('/');
    }

    return $this->renderer->render($response, 'input.phtml', [
        'input' => $input
    ]);
});
