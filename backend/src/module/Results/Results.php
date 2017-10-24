<?php

namespace App\Results;

use App\AbstractModule;
use App\Helper;
use App\RenderableInterface;
use PDO;

class Results extends AbstractModule implements RenderableInterface
{

    /**
     * Default page limit
     *
     * @var int
     */
    private $limit = 10;


    public function getTitle(): string
    {
        return 'Results';
    }

    public function render($data = []): string
    {
        $input  = $_GET['i'];
        $offset = isset($_GET['o']) ? (int) $_GET['o'] : 0;

        if (preg_match('/topic:([0-9]+)/', $_GET['i'], $matches)) {
            $output = Helper::runCommand('topic.py', $matches[1]);
        } else {
            $output = Helper::runCommand('query.py', $_GET['i']);
        }

        if (trim($output) == '') {
            $outputIds = [];
            $ids = [];
        } else {
            $outputIds = array_map('intval', explode(',', trim($output)));
            $ids = array_slice($outputIds, $offset, $this->limit);
        }

        // Retrieve info for documents

        /** @var PDO $connection */
        $container  = $this->getContainer();
        $connection = $container[PDO::class];

        $statement  = $connection->prepare('SELECT *, GROUP_CONCAT(authors.name) as authors_name, GROUP_CONCAT(authors.id) as authors_id from papers INNER JOIN paper_authors ON papers.id=paper_authors.paper_id INNER JOIN authors ON paper_authors.author_id=authors.id WHERE papers.id=:id GROUP BY papers.id');

        $documents = [];
        foreach ($ids as $id) {
            $statement->execute([':id' => $id]);

            $row = $statement->fetch(PDO::FETCH_ASSOC);

            $documents[$id]["title"] = htmlspecialchars($row['title']);
            $documents[$id]["year"] = $row['year'];
            $documents[$id]["pdf_name"] = $row['pdf_name'];
            $documents[$id]["abstract"] = $row["abstract"];

            $authorIds = explode(",", $row['authors_id']);
            $authorNames = explode(",", htmlspecialchars($row['authors_name']));

            $documents[$id]["authors"] = [];

            foreach ($authorIds as $i => $authorId) {
                $documents[$id]["authors"][] = [
                  'id' => $authorId,
                  'name' => $authorNames[$i]
                ];
            }
        }

        // Retrieve stemmed input

        $output = Helper::runCommand('stem.py', $_GET['i']);

        $stemmed = explode(" ", $output);
        $stemmed = array_filter($stemmed, function ($var) {
            return !in_array($var, ['not', 'and', 'or']);
        });

        return Helper::render(__DIR__ . '/view/results.phtml', [
            'input'   => $input,
            'total'   => count($outputIds),
            'offset'  => $offset,
            'limit'   => $this->limit,
            'results' => $documents,
            'stemmed' => $stemmed,
        ]);
    }
}