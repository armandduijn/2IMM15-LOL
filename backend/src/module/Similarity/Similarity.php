<?php

namespace App\Similarity;

use App\AbstractModule;
use App\RenderableInterface;

class Similarity extends AbstractModule implements RenderableInterface
{
    public function getTitle(): string
    {
        return 'Similarity';
    }

    public function render($data = []): string
    {
        $command = "python \"".getcwd()."/../../modules/view-helpers/query.py\" ";
        $command .= escapeshellarg($_GET['i']);
        //print($command);
        //var_dump($command);
        $output = shell_exec($command);

        $data = [
            'output' => $output
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/similarity.phtml';

        return ob_get_clean();
    }
}