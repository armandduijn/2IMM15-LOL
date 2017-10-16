<?php

use Slim\Http\Request;
use Slim\Http\Response;
use App\Author\Model as Author;

$app->get('/api/suggest', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    $response = $response->withHeader('Content-type', 'application/json');
    
    $suggestions = new \App\Suggestion\Suggestion();
    $response = $response->withJson($suggestions->giveSuggestions($params['q']));
    return $response;
});

$app->get('/[{name}]', function ($request, $response, $args) {
    // Sample log message
    $this->logger->info("Slim-Skeleton '/' route");

    // Render index view
    return $this->renderer->render($response, 'index.phtml', $args);
});


$app->get('/input/', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();

    $input = $params['i'] ?? null;
    $command = "python \"".getcwd()."/../../modules/view-helpers/stem.py\" ";
    $command .= escapeshellarg($_GET['i']);
    $stemmed = shell_exec($command);

    if (is_null($input) || empty($input)) {
        return $response->withRedirect('/');
    }

    $collaborationModule = new \App\Author\Collaboration();
    $collaborationModule->setContainer($this);

    $similarityModule = new \App\Similarity\Similarity();
    $similarityModule->setContainer($this);

//    $components[] = new \App\Author\Collaboration();
    $components[] = $similarityModule;
    $components[] = new \App\Results\Results();

    $data = [ 'authors' => [], 'collaborations' => [] ];

    $author = new Author();
    $author->setContainer($this);
    $author->loadByName('Tomaso Poggio');

    $author2 = new Author();
    $author2->setContainer($this);
    $author2->loadByName('Thomas Serre');

    $author3 = new Author();
    $author3->setContainer($this);
    $author3->loadByName('Massimiliano Pontil');

    return $this->renderer->render($response, 'input.phtml', [
        'input'      => $input,
        'stemmedInput' => $stemmed,
        'components' => $components
    ]);
});
