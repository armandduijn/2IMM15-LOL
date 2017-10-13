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
        $statement = $db->prepare('SELECT *, GROUP_CONCAT(authors.name) as authors from papers INNER JOIN paper_authors ON papers.id=paper_authors
.paper_id INNER JOIN authors ON paper_authors.author_id=authors.id WHERE papers.id=:id GROUP BY papers.id');
        $documents = [];
        foreach ($documentIds as $id) {
            $statement->execute([':id' => $id]);
            $row = $statement->fetch(PDO::FETCH_ASSOC);
            $documents[$id]["title"] = $row['title'];
            $documents[$id]["year"] = $row['year'];
            $documents[$id]["authors"] = explode(",", $row['authors']);
        }
        
        $data = [
            $results = $documents,
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/results.phtml';

        return ob_get_clean();
    }
}