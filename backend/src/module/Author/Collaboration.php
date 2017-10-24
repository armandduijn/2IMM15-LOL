<?php

namespace App\Author;

use App\AbstractModule;
use App\Helper;
use App\RenderableInterface;
use App\Author\Model as Author;
use App\Paper\Model as Paper;
use PDO;

class Collaboration extends AbstractModule implements RenderableInterface
{
    private $authors;

    private $collaborations;

    /**
     * Collaboration constructor.
     */
    public function __construct(array $authors)
    {
        $this->authors = $authors;
        $this->collaborations = $this->papers();
    }

    /**
     * @return Paper[]
     * @throws \Psr\Container\ContainerExceptionInterface
     * @throws \Psr\Container\NotFoundExceptionInterface
     */
    public function papers(): array
    {
        if (count($this->authors) < 2) {
            return [];
        }

        /** @var PDO $connection */
        $connection = $this->getContainer()->get(PDO::class);

        // Who an author has worked with and on which paper
        $sql = 'SELECT
                  A1.id,
                  A1.name,
                  PA2.author_id AS collaborator_id,
                  A2.name       AS collaborator_name,
                  PA1.paper_id  AS collaboration_paper_id,
                  P.title       AS collaboration_paper_title
                FROM authors AS A1, authors AS A2, paper_authors AS PA1, paper_authors AS PA2, papers AS P
                WHERE A1.id = :firstAuthorId
                  AND PA1.author_id = A1.id
                  AND PA2.author_id = A2.id
                  AND PA2.author_id <> A1.id
                  AND PA1.paper_id = PA2.paper_id
                  AND PA1.paper_id = P.id
                ORDER BY A1.id';

        $statement = $connection->prepare($sql);
        $statement->execute([
            ':firstAuthorId' => $this->authors[0]->getId()
        ]);

        $collaborators = array_map(function (Author $author) {
            return $author->getId();
        }, $this->authors);

        // Generate a list of papers that have been contributed on by the provided authors
        $collaborations = array_filter($statement->fetchAll(), function ($collaboration) use ($collaborators) {
            return in_array($collaboration['collaborator_id'], $collaborators);
        });

        // Map the data to Paper objects
        $papers = array_map(function ($collaboration) {
            return new Paper([
                'id'    => $collaboration['collaboration_paper_id'],
                'title' => $collaboration['collaboration_paper_title']
            ]);
        }, $collaborations);

        // Count the amount of times a paper has been collaborated on
        // A higher occurence signifies that multiple of the provided authors have worked on it
        $occurrences = array_count_values(array_map(function (Paper $paper) {
            return $paper->getId();
        }, $papers));

        // Generate a list of papers that have been worked on by all the authors
        $intersect = array_filter($papers, function (Paper $paper) use ($occurrences) {
            // An author can't collaborate with himself
            return $occurrences[$paper->getId()] === (count($this->authors) - 1);
        });

        return array_unique($intersect);
    }

    public function getTitle(): string
    {
        return 'Collaborations';
    }

    public function render($data = []): string
    {
        return Helper::render(__DIR__ . '/view/collaboration.phtml', [
            'authors'        => $this->authors,
            'collaborations' => $this->collaborations,
        ]);
    }
}
