<?php

namespace App;

use Psr\Container\ContainerInterface;

abstract class AbstractModule
{
    protected $title = 'Component';

    /**
     * @return ContainerInterface
     */
    protected function getContainer(): ContainerInterface
    {
        return Container::getContainer();
    }

    public function getTitle(): string
    {
        return $this->title;
    }

    /**
     * @param string $title
     */
    public function setTitle(string $title)
    {
        $this->title = $title;
    }
}
