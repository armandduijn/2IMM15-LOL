<?php

namespace App\Results;

use App\AbstractModule;
use App\RenderableInterface;
use PDO;

class Results extends AbstractModule implements RenderableInterface
{
    public function getTitle(): string
    {
        return 'Results';
    }

    public function render($data = []): string
    {
        
        $command = "python \"".getcwd()."/../../modules/view-helpers/query.py\" ";
        $command .= escapeshellarg($_GET['i']);
        //print($command);
        //var_dump($command);
        $output = shell_exec($command);
        
        $documentIds = array_map('intval', explode(',', $output));
    
        // retrieve info for documents
        $db = new PDO('sqlite:'.__DIR__.'/../../../../data/database.sqlite');
        $statement = $db->prepare('SELECT *, GROUP_CONCAT(authors.name) as authors_name, GROUP_CONCAT(authors.id) as authors_id from papers INNER JOIN paper_authors ON papers.id=paper_authors.paper_id INNER JOIN authors ON paper_authors.author_id=authors.id WHERE papers.id=:id GROUP BY papers.id');
        $documents = [];
        foreach ($documentIds as $id) {
            $statement->execute([':id' => $id]);
            $row = $statement->fetch(PDO::FETCH_ASSOC);
            $documents[$id]["title"] = htmlspecialchars($row['title']);
            $documents[$id]["year"] = $row['year'];
            $documents[$id]["pdf_name"] = $row['pdf_name'];
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
    
        $command = "python \"".getcwd()."/../../modules/view-helpers/stem.py\" ";
        $command .= escapeshellarg($_GET['i']);
        $stemmedInput = explode(" ", shell_exec($command));
        $stemmedInput = array_filter($stemmedInput, function($var) { return !in_array($var, ['not', 'and', 'or']);});
        
        $data = [
            $results = $documents,
            $stemmedInput
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/results.phtml';

        return ob_get_clean();
    }
}