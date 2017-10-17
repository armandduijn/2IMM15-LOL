<?php

namespace App\Similarity;

use App\AbstractModule;
use App\Helper;
use App\RenderableInterface;

class Similarity extends AbstractModule implements RenderableInterface
{
    /**
     * Author ID
     *
     * @var string
     */
    protected $authorId;

    /**
     * Similarity constructor.
     */
    public function __construct()
    {
        $this->setTitle('Similarity');
    }

    /**
     * @inheritdoc
     */
    public function render($data = []): string
    {
        $output = Helper::runCommand('similarity.py', $this->getAuthorId());

        $pattern = "/\('([0-9]+)', ([0|1].[0-9]+)\)/";
        $results = [];

        if (preg_match_all($pattern, $output, $matches)) {
            $results = array_map(null, $matches[1], $matches[2]);
        };

        // Limit the amount of results
        // The first result (100%) is the author himself.
        $results = array_slice($results, 1, 5);

        // Filter out irrelevant authors (below a threshold)
        $results = array_filter($results, function ($result) {
            $threshold = 0.5;

            return ((float) $result[1]) >= $threshold;
        });

        // Generate an array of only author IDs
        $ids = array_map(function ($result) {
            return $result[0];
        }, $results);

        /** @var \PDO $connection */
        $connection = $this->getContainer()->get(\PDO::class);
        $statement = $connection->query('SELECT * FROM authors WHERE id IN (' . implode(',', $ids) . ')');

        foreach ($statement->fetchAll() as $index => $data) {
            // Assume that the results are in the correct order
            $results[$index][] = $data['name'];
        }

        return Helper::render(__DIR__ . '/view/similarity.phtml', [
            'results' => $results
        ]);
    }

    /**
     * @return string
     */
    public function getAuthorId(): string
    {
        return $this->authorId;
    }

    /**
     * @param string $authorId
     */
    public function setAuthorId(string $authorId)
    {
        $this->authorId = $authorId;
    }
}
