<?php

namespace App\Suggestion;

use App\AbstractModule;
use App\RenderableInterface;
use PDO;

class Suggestion extends AbstractModule
{
    public function getTitle(): string
    {
        return 'Suggestion';
    }
    
    public function giveSuggestions($query) {
        return [
          $query,
          $query.' toch?'
        ];
    }
}