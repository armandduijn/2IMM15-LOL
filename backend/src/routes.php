<?php

use App\Author\Collaboration as CollaborationComponent;
use App\Similarity\Similarity as SimilarityComponent;
use App\Topic\Topic as TopicComponent;
use App\Container;
use App\Helper;
use App\Results\Results as ResultsComponent;
use Slim\Http\Request;
use Slim\Http\Response;
use App\Author\Model as Author;

$app->get('/', function ($request, $response) {
    return $this->renderer->render($response, 'index.phtml');
});

$app->get('/api/suggest', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();

    $suggestions = new \App\Suggestion\Suggestion();

    return $response->withJson($suggestions->giveSuggestions($params['q']));
});

$app->get('/input/', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    $input = $params['i'] ?? null;

    if (is_null($input) || empty($input)) {
        return $response->withRedirect('/');
    }

    Container::setContainer($this);

    $components = [];
    $topicShown = false;

    // If multiple `author:$id` patterns are found in the input
    if (preg_match_all('/author:([0-9]+)/', $input, $matches)) {
        if (count($matches[1]) >= 2) {
            $authors = [];

            foreach ($matches[1] as $id) {
                $authors[] = Author::find($id);
            }

            $component = new CollaborationComponent($authors);

            $components[] = $component;
        }
    }

    // If a `author:$id` is found in the input
    if (preg_match('/author:([0-9]+)/', $input, $matches)) {
        $component = new SimilarityComponent();
        $component->setAuthorId($matches[1]);

        $components[] = $component;

        $component = new TopicComponent();
        $component->setAuthorId($matches[1]);

        $components[] = $component;

        $topicShown = true;
    }

    //If 'topic:$id' is found in the input
    if (preg_match('/topic:([0-9]+)/', $input, $matches)) {
        $component = new TopicComponent();
        $component->setTopicId($matches[1]);

        $components[] = $component;

        $component = new TopicComponent();
        $component->setTopicId($matches[1]);
        $component->setStats(true);

        $components[] = $component;

        $topicShown = true;
    }

    //If 'year:$val' is found in the input
    if (preg_match('/year:([0-9]+)/', $input, $matches)) {
        $component = new TopicComponent();
        $component->setYear($matches[1]);

        $components[] = $component;

        $component = new TopicComponent();
        $component->setYear($matches[1]);
        $component->setStats(true);

        $components[] = $component;

        $topicShown = true;
    }

    // If some condition is met
    if (true) {
        if (!$topicShown) {
            $component = new TopicComponent();
            $components[] = $component;
        }

        $component = new ResultsComponent();

        $components[] = $component;
    }

    return $this->renderer->render($response, 'input.phtml', [
        'input'        => $input,
        'components'   => $components,
    ]);
});
