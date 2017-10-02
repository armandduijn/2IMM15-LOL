<?php

namespace App\Results;

use App\Renderable;

class Results implements Renderable
{
    public function getTitle(): string
    {
        return 'Results';
    }

    public function render(): string
    {
        $data = [
        
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/results.phtml';

        return ob_get_clean();
    }
}