<?php

namespace App\Author;

class Model
{
    private $name;

    public function __construct(string $name)
    {
        $this->name = $name;
    }

    public function getId(): int
    {
        return 1;
    }

    public function getName(): string
    {
        return $this->name;
    }
}
