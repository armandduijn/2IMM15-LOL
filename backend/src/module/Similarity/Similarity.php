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
