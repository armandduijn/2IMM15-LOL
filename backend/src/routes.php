<?php

use App\Author\Collaboration as CollaborationComponent;
use App\Container;
use App\Helper;
use App\Results\Results as ResultsComponent;
use Slim\Http\Request;
use Slim\Http\Response;
use App\Author\Model as Author;

use App\Similarity\Similarity as SimilarityComponent;

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

    Container::setContainer($this);

    $components = [];

    // If `author:$id` is found in the input
    if (preg_match('/author:([0-9]+)/', $input, $matches)) {
        $component = new SimilarityComponent();
        $component->setAuthorId($matches[1]);

        $components[] = $component;
    }

    // If some condition is met
    if (false) {
        $component = new CollaborationComponent();

        $components[] = $component;
    }

    // If some condition is met
    if (true) {
        $component = new ResultsComponent();

        $components[] = $component;
    }

    return $this->renderer->render($response, 'input.phtml', [
        'input'        => $input,
        'stemmedInput' => Helper::runCommand('stem.py', $input),
        'components'   => $components,
    ]);
});
