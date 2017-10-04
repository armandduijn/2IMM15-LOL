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
        $data = [
        
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/results.phtml';

        return ob_get_clean();
    }
}