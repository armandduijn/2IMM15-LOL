<?php

namespace App\Author;

use App\Renderable;
use App\Author\Model as Author;
use App\Paper\Model as Paper;

class Collaboration implements Renderable
{
    public function getTitle(): string
    {
        return 'Collaboration';
    }

    public function render(): string
    {
        $data = [
            'authors'        => [ new Author('Pietje'), new Author('Ja2n') ],
            'collaborations' => [ new Paper(), new Paper(), new Paper(), new Paper() ]
        ];

        ob_start();

        extract($data);
        include __DIR__ . '/view/collaboration.phtml';

        return ob_get_clean();
    }
}